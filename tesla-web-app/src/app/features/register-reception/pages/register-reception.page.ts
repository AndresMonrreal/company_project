import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { RegisterReceptionApiClient } from '../data-access/register-reception-api.client';
import { ContainerOption, ProfileOption, ReceptionSuccessResponse } from '../models/register-reception.models';

@Component({
  selector: 'app-register-reception-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Register Reception</h1>
        <p class="text-gray-500 mt-1 text-sm">Record a new material reception into inventory.</p>
      </div>

      @if (successResponse()) {
        <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">
          Reception registered —
          Container: <strong>{{ successResponse()!.containerCode }}</strong>,
          Profile: <strong>{{ successResponse()!.profileCode }}</strong>,
          Lot: <strong>{{ successResponse()!.lot }}</strong>,
          Qty: <strong>{{ successResponse()!.receivedQuantity }}</strong>
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
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-6 max-w-lg">
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-5">

            <div>
              <label for="containerId" class="block text-sm font-medium text-gray-700 mb-1">Container</label>
              <select id="containerId" formControlName="containerId"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white">
                <option [ngValue]="null" disabled>Select a container</option>
                @for (container of containers(); track container.id) {
                  <option [ngValue]="container.id">{{ container.code }}</option>
                }
              </select>
            </div>

            <div>
              <label for="profileId" class="block text-sm font-medium text-gray-700 mb-1">Profile</label>
              <select id="profileId" formControlName="profileId"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white">
                <option [ngValue]="null" disabled>Select a profile</option>
                @for (profile of profiles(); track profile.id) {
                  <option [ngValue]="profile.id">{{ profile.code }} — {{ profile.name }}</option>
                }
              </select>
            </div>

            <div>
              <label for="lot" class="block text-sm font-medium text-gray-700 mb-1">Lot</label>
              <input id="lot" type="text" formControlName="lot"
                     placeholder="Scan or type lot barcode"
                     class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>

            <div>
              <label for="receivedQuantity" class="block text-sm font-medium text-gray-700 mb-1">Received Quantity</label>
              <input id="receivedQuantity" type="number" formControlName="receivedQuantity"
                     min="1" step="1"
                     placeholder="Enter quantity"
                     class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            </div>

            <button type="submit"
                    [disabled]="catalogsLoading() || submitting() || form.invalid"
                    class="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
              @if (submitting()) {
                <span class="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent"></span>
                Registering...
              } @else {
                Register Reception
              }
            </button>

          </form>
        </div>
      }
    </div>
  `,
})
export class RegisterReceptionPageComponent implements OnInit {
  private readonly api = inject(RegisterReceptionApiClient);
  private readonly fb = inject(FormBuilder);

  readonly containers = signal<ContainerOption[]>([]);
  readonly profiles = signal<ProfileOption[]>([]);
  readonly catalogsLoading = signal(true);
  readonly submitting = signal(false);
  readonly successResponse = signal<ReceptionSuccessResponse | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    containerId: [null as number | null, Validators.required],
    profileId: [null as number | null, Validators.required],
    lot: ['', [Validators.required, Validators.pattern(/\S+/)]],
    receivedQuantity: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    forkJoin({
      containers: this.api.getContainers(),
      profiles: this.api.getProfiles(),
    }).subscribe({
      next: ({ containers, profiles }) => {
        this.containers.set(containers);
        this.profiles.set(profiles);
        this.catalogsLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load catalogs. Please refresh.');
        this.catalogsLoading.set(false);
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.submitting() || this.catalogsLoading()) return;

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successResponse.set(null);

    const { containerId, profileId, lot, receivedQuantity } = this.form.getRawValue();

    this.api.register({
      containerId: containerId!,
      profileId: profileId!,
      lot: lot!.trim(),
      receivedQuantity: receivedQuantity!,
    }).subscribe({
      next: (response) => {
        this.successResponse.set(response);
        this.submitting.set(false);
        this.form.reset();
        setTimeout(() => this.successResponse.set(null), 3000);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if (err.status === 400) {
          this.errorMessage.set('Please check the form fields.');
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
