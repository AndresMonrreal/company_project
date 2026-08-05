import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { RegisterCutApiClient } from '../data-access/register-cut-api.client';
import { ShiftApiClient } from '../../my-activity/data-access/shift-api.client';
import { CutSuccessResponse, InventoryAvailableResult, MachineOption } from '../models/register-cut.models';

@Component({
  selector: 'app-register-cut-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Register Cut</h1>
        <p class="text-gray-500 mt-1 text-sm">Scan a container to load inventory, then record a cutting operation.</p>
      </div>

      @if (successResponse()) {
        <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">
          Cut registered —
          Container: <strong>{{ successResponse()!.containerCode }}</strong>,
          Profile: <strong>{{ successResponse()!.profileCode }}</strong>,
          Machine: <strong>{{ successResponse()!.machineName }}</strong>,
          Good: <strong>{{ successResponse()!.goodQuantity }}</strong>,
          Scrap: <strong>{{ successResponse()!.scrapQuantity }}</strong>
        </div>
      }

      @if (errorMessage()) {
        <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {{ errorMessage() }}
        </div>
      }

      @if (catalogsLoading()) {
        <div class="flex justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
        </div>
      } @else {
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 max-w-lg space-y-5">

          <!-- Container code scanner input -->
          <div>
            <label for="lot" class="block text-sm font-medium text-gray-700 mb-1">Lot Number</label>
            <div class="flex gap-2">
              <input id="lot" type="text"
                     [value]="lotValue()"
                     (input)="updateLotValue($event)"
                     (keydown.enter)="$event.preventDefault(); onLotLookup()"
                     placeholder="Scan or type lot number"
                     class="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              <button type="button" (click)="onLotLookup()"
                      [disabled]="lookupLoading()"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                @if (lookupLoading()) {
                  <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent inline-block"></span>
                } @else {
                  Search
                }
              </button>
            </div>
            @if (inventoryError()) {
              <p class="mt-1 text-xs text-red-600">{{ inventoryError() }}</p>
            }
          </div>

          <!-- Inventory summary card -->
          @if (inventoryResult()) {
            <div class="rounded-lg border border-indigo-100 bg-indigo-50 p-4 text-sm space-y-1">
              <p class="font-semibold text-indigo-900">{{ inventoryResult()!.containerCode }}</p>
              <p class="text-indigo-700">Profile: {{ inventoryResult()!.profileCode }}</p>
              <p class="text-indigo-700">Lot: {{ inventoryResult()!.lot }}</p>
              <p class="text-indigo-700">Available: <strong>{{ inventoryResult()!.availableQuantity }}</strong></p>
            </div>

            <!-- Cut form -->
            <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-5">

              <div>
                <label for="machineId" class="block text-sm font-medium text-gray-700 mb-1">Machine</label>
                <select id="machineId" formControlName="machineId"
                        class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white">
                  <option [ngValue]="null" disabled>Select a machine</option>
                  @for (machine of machines(); track machine.id) {
                    <option [ngValue]="machine.id">{{ machine.name }}</option>
                  }
                </select>
              </div>

              <div>
                <label for="initialQuantity" class="block text-sm font-medium text-gray-700 mb-1">Initial Quantity</label>
                <input id="initialQuantity" type="number" formControlName="initialQuantity"
                       min="1" step="1" placeholder="Total pieces"
                       class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label for="goodQuantity" class="block text-sm font-medium text-gray-700 mb-1">Good Quantity</label>
                  <input id="goodQuantity" type="number" formControlName="goodQuantity"
                         min="0" step="1" placeholder="Good pieces"
                         class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
                <div>
                  <label for="scrapQuantity" class="block text-sm font-medium text-gray-700 mb-1">Scrap Quantity</label>
                  <input id="scrapQuantity" type="number" formControlName="scrapQuantity"
                         min="0" step="1" placeholder="Scrap pieces"
                         class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                </div>
              </div>

              @if (!quantitiesMatch()) {
                <p class="rounded-lg border border-yellow-200 bg-yellow-50 px-3 py-2 text-xs text-yellow-800">
                  Quantities do not add up: initial must equal good + scrap
                </p>
              }

              @if (showScrapReason()) {
                <div>
                  <label for="scrapReason" class="block text-sm font-medium text-gray-700 mb-1">
                    Scrap Reason <span class="text-red-500">*</span>
                  </label>
                  <textarea id="scrapReason" formControlName="scrapReason" rows="2" maxlength="255"
                            placeholder="Describe reason for scrap…"
                            class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"></textarea>
                </div>
              }

              <button type="submit"
                      [disabled]="submitting() || form.invalid || !quantitiesMatch() || !currentShiftId()"
                      class="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
                @if (submitting()) {
                  <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                  Registering...
                } @else {
                  Register Cut
                }
              </button>

            </form>
          }

        </div>
      }
    </div>
  `,
})
export class RegisterCutPageComponent implements OnInit {
  private readonly api = inject(RegisterCutApiClient);
  private readonly shiftApi = inject(ShiftApiClient);
  private readonly fb = inject(FormBuilder);

  readonly machines = signal<MachineOption[]>([]);
  readonly currentShiftId = signal<number | null>(null);
  readonly catalogsLoading = signal(true);
  readonly lotValue = signal('');
  readonly lookupLoading = signal(false);
  readonly inventoryResult = signal<InventoryAvailableResult | null>(null);
  readonly inventoryError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly successResponse = signal<CutSuccessResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly showScrapReason = signal(false);

  readonly form = this.fb.group({
    machineId: [null as number | null, Validators.required],
    initialQuantity: [null as number | null, [Validators.required, Validators.min(1)]],
    goodQuantity: [null as number | null, [Validators.required, Validators.min(0)]],
    scrapQuantity: [null as number | null, [Validators.required, Validators.min(0)]],
    scrapReason: [null as string | null],
  });

  ngOnInit(): void {
    forkJoin({
      machines: this.api.getMachines(),
      shift: this.shiftApi.getCurrentShift(),
    }).subscribe({
      next: ({ machines, shift }) => {
        this.machines.set(machines);
        this.currentShiftId.set(shift.id);
        this.catalogsLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load machines or shift. Please refresh.');
        this.catalogsLoading.set(false);
      },
    });

    this.form.get('scrapQuantity')!.valueChanges.subscribe(qty => {
      const hasScrap = qty != null && qty > 0;
      this.showScrapReason.set(hasScrap);
      const reasonCtrl = this.form.get('scrapReason')!;
      if (hasScrap) {
        reasonCtrl.setValidators([Validators.required, Validators.maxLength(255)]);
      } else {
        reasonCtrl.clearValidators();
        reasonCtrl.setValue(null);
      }
      reasonCtrl.updateValueAndValidity();
    });
  }

  protected updateLotValue(event: Event): void {
    this.lotValue.set((event.target as HTMLInputElement).value);
  }

  protected onLotLookup(): void {
    const code = this.lotValue().trim();
    if (!code) return;

    this.lookupLoading.set(true);
    this.inventoryResult.set(null);
    this.inventoryError.set(null);
    this.form.reset();
    this.showScrapReason.set(false);

    this.api.getInventoryAvailable(code).subscribe({
      next: (result) => {
        this.inventoryResult.set(result);
        this.lookupLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.lookupLoading.set(false);
        if (err.status === 404) {
          this.inventoryError.set(`No available reception found for lot: ${code}`);
        } else {
          this.inventoryError.set('Failed to load inventory. Please try again.');
        }
      },
    });
  }

  protected quantitiesMatch(): boolean {
    const { initialQuantity, goodQuantity, scrapQuantity } = this.form.getRawValue();
    if (initialQuantity === null || goodQuantity === null || scrapQuantity === null) return true;
    return initialQuantity === goodQuantity + scrapQuantity;
  }

  onSubmit(): void {
    const inventory = this.inventoryResult();
    const shiftId = this.currentShiftId();
    if (this.form.invalid || this.submitting() || !inventory || !shiftId || !this.quantitiesMatch()) return;

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successResponse.set(null);

    const { machineId, initialQuantity, goodQuantity, scrapQuantity, scrapReason } = this.form.getRawValue();

    this.api.register({
      inventoryItemId: inventory.inventoryItemId,
      machineId: machineId!,
      shiftId,
      initialQuantity: initialQuantity!,
      goodQuantity: goodQuantity!,
      scrapQuantity: scrapQuantity!,
      scrapReason: scrapReason ?? null,
    }).subscribe({
      next: (response) => {
        this.successResponse.set(response);
        this.submitting.set(false);
        this.form.reset();
        this.showScrapReason.set(false);
        this.inventoryResult.set(null);
        this.lotValue.set('');
        setTimeout(() => this.successResponse.set(null), 3000);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if (err.status === 422) {
          const code = err.error?.code;
          if (code === 'cutting.scrap-reason-required') {
            this.errorMessage.set('Scrap reason is required when scrap quantity is greater than zero.');
          } else {
            this.errorMessage.set('Quantities do not add up: initial must equal good + scrap.');
          }
        } else if (err.status === 401) {
          this.errorMessage.set('Session expired. Please log in again.');
        } else if (err.status === 403) {
          this.errorMessage.set('Insufficient permissions.');
        } else {
          this.errorMessage.set('Unexpected error. Please try again.');
        }
      },
    });
  }
}