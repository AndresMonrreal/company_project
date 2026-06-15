import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { RegisterReceptionService } from '../services/register-reception.service';
import {
  ContainerOption,
  ProfileOption,
  ReceptionSuccessResponse,
} from '../models/register-reception.models';

@Component({
  selector: 'app-register-reception-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold text-gray-900">Register Reception</h1>
      </div>

      <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-8">
        @if (catalogsLoading()) {
          <div class="flex items-center justify-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
          </div>
        }

        @if (!catalogsLoading()) {
          @if (successResponse()) {
            <div class="rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800 mb-6">
              Reception registered: {{ successResponse()!.containerCode }} / {{ successResponse()!.profileCode }} — Lot {{ successResponse()!.lot }}, {{ successResponse()!.receivedQuantity }} pcs
            </div>
          }

          @if (errorMessage()) {
            <div class="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 mb-6">
              {{ errorMessage() }}
            </div>
          }

          <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-5">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1" for="containerId">Container</label>
              <select
                id="containerId"
                formControlName="containerId"
                class="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white text-gray-900"
              >
                <option [ngValue]="null" disabled selected>Select...</option>
                @for (c of containers(); track c.id) {
                  <option [ngValue]="c.id">{{ c.code }}</option>
                }
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1" for="profileId">Profile</label>
              <select
                id="profileId"
                formControlName="profileId"
                class="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white text-gray-900"
              >
                <option [ngValue]="null" disabled selected>Select...</option>
                @for (p of profiles(); track p.id) {
                  <option [ngValue]="p.id">{{ p.code }}</option>
                }
              </select>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1" for="lot">Lot</label>
              <input
                id="lot"
                type="text"
                formControlName="lot"
                class="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white text-gray-900"
                placeholder="Enter lot number"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1" for="receivedQuantity">Received Quantity</label>
              <input
                id="receivedQuantity"
                type="number"
                formControlName="receivedQuantity"
                class="w-full border border-gray-300 rounded-lg px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white text-gray-900"
                placeholder="Enter quantity"
                min="1"
              />
            </div>

            <button
              type="submit"
              [disabled]="catalogsLoading() || submitting() || form.invalid"
              class="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg py-2.5 transition-colors"
            >
              {{ submitting() ? 'Registering...' : 'Register Reception' }}
            </button>
          </form>
        }
      </div>
    </div>
  `,
})
export class RegisterReceptionPageComponent implements OnInit {
  private readonly service = inject(RegisterReceptionService);

  protected readonly containers = signal<ContainerOption[]>([]);
  protected readonly profiles = signal<ProfileOption[]>([]);
  protected readonly catalogsLoading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly successResponse = signal<ReceptionSuccessResponse | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = new FormGroup({
    containerId: new FormControl<number | null>(null, { validators: [Validators.required], nonNullable: false }),
    profileId: new FormControl<number | null>(null, { validators: [Validators.required], nonNullable: false }),
    lot: new FormControl('', { validators: [Validators.required, Validators.minLength(1)], nonNullable: true }),
    receivedQuantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(1)], nonNullable: false }),
  });

  ngOnInit(): void {
    forkJoin({
      containers: this.service.loadContainers(),
      profiles: this.service.loadProfiles(),
    }).subscribe({
      next: ({ containers, profiles }) => {
        this.containers.set(containers);
        this.profiles.set(profiles);
        this.catalogsLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load form data. Please refresh the page.');
        this.catalogsLoading.set(false);
      },
    });
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.successResponse.set(null);

    const { containerId, profileId, lot, receivedQuantity } = this.form.value;

    this.service.register({
      containerId: containerId!,
      profileId: profileId!,
      lot: lot!,
      receivedQuantity: receivedQuantity!,
    }).subscribe({
      next: response => {
        this.successResponse.set(response);
        this.submitting.set(false);
        this.form.reset();
        setTimeout(() => this.successResponse.set(null), 3000);
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        if (err instanceof HttpErrorResponse) {
          if (err.status === 400) {
            this.errorMessage.set('Please check the form fields and try again.');
          } else if (err.status === 401) {
            this.errorMessage.set('Your session has expired. Please log in again.');
          } else if (err.status === 403) {
            this.errorMessage.set('You do not have permission to register receptions.');
          } else if (err.status === 409) {
            this.errorMessage.set('This container already has an active reception.');
          } else {
            this.errorMessage.set('An unexpected error occurred. Please try again.');
          }
        } else {
          this.errorMessage.set('An unexpected error occurred. Please try again.');
        }
      },
    });
  }
}
