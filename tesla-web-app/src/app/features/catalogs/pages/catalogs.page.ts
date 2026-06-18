import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { CatalogsApiClient } from '../data-access/catalogs-api.client';
import { CatalogContainer, ContainerType, Machine, Profile, Shift } from '../models/catalogs.models';

type ActiveTab = 'profiles' | 'machines' | 'shifts' | 'containerTypes' | 'containers';

@Component({
  selector: 'app-catalogs-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Catalogs</h1>
        <p class="text-gray-500 mt-1 text-sm">Manage system catalogs.</p>
      </div>

      <!-- Tab bar -->
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex space-x-6 overflow-x-auto">
          @for (tab of tabs; track tab.key) {
            <button
              (click)="activeTab.set(tab.key)"
              class="pb-3 px-1 text-sm font-medium border-b-2 whitespace-nowrap transition-colors"
              [class]="activeTab() === tab.key
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
            >
              {{ tab.label }}
            </button>
          }
        </nav>
      </div>

      <!-- ===== PROFILES TAB ===== -->
      @if (activeTab() === 'profiles') {
        <div class="space-y-4">
          @if (profilesError()) {
            <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ profilesError() }}</div>
          }
          @if (profilesSuccess()) {
            <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">{{ profilesSuccess() }}</div>
          }

          @if (profilesLoading()) {
            <div class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
            </div>
          } @else {
            @if (!showProfileForm() && editingProfileId() === null) {
              <div>
                <button
                  (click)="showProfileForm.set(true)"
                  class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors"
                >
                  + Add Profile
                </button>
              </div>
            }

            @if (showProfileForm()) {
              <div class="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-3">
                <h3 class="text-sm font-semibold text-gray-700">Add Profile</h3>
                <form [formGroup]="profileCreateForm" (ngSubmit)="submitProfileCreate()" class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">Code *</label>
                    <input type="text" formControlName="code" maxlength="10"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">Name *</label>
                    <input type="text" formControlName="name" maxlength="100"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2">
                    <label class="block text-xs font-medium text-gray-600 mb-1">Description</label>
                    <input type="text" formControlName="description" maxlength="255"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2 flex gap-2">
                    <button type="submit" [disabled]="profileSubmitting() || profileCreateForm.invalid"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                      @if (profileSubmitting()) { Saving… } @else { Save }
                    </button>
                    <button type="button" (click)="cancelProfileForm()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            }

            @if (editingProfileId() !== null) {
              <div class="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-3">
                <h3 class="text-sm font-semibold text-gray-700">Edit Profile</h3>
                <form [formGroup]="profileEditForm" (ngSubmit)="submitProfileEdit()" class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div class="sm:col-span-2">
                    <label class="block text-xs font-medium text-gray-600 mb-1">Name *</label>
                    <input type="text" formControlName="name" maxlength="100"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2">
                    <label class="block text-xs font-medium text-gray-600 mb-1">Description</label>
                    <input type="text" formControlName="description" maxlength="255"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2 flex gap-2">
                    <button type="submit" [disabled]="profileSubmitting() || profileEditForm.invalid"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                      @if (profileSubmitting()) { Saving… } @else { Update }
                    </button>
                    <button type="button" (click)="cancelProfileForm()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            }

            <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Code</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Name</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Description</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (item of profiles(); track item.id) {
                    <tr class="hover:bg-gray-50">
                      <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ item.code }}</td>
                      <td class="px-4 py-3 text-sm text-gray-900">{{ item.name }}</td>
                      <td class="px-4 py-3 text-sm text-gray-500">{{ item.description }}</td>
                      <td class="px-4 py-3">
                        <div class="flex gap-2">
                          <button (click)="startEditProfile(item)"
                            class="text-xs rounded px-2 py-1 bg-yellow-50 text-yellow-700 hover:bg-yellow-100 border border-yellow-200 transition-colors">Edit</button>
                          <button (click)="deleteProfile(item.id)"
                            class="text-xs rounded px-2 py-1 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition-colors">Delete</button>
                        </div>
                      </td>
                    </tr>
                  }
                  @if (profiles().length === 0) {
                    <tr><td colspan="4" class="px-4 py-8 text-center text-sm text-gray-400">No profiles found.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }

      <!-- ===== MACHINES TAB ===== -->
      @if (activeTab() === 'machines') {
        <div class="space-y-4">
          @if (machinesError()) {
            <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ machinesError() }}</div>
          }
          @if (machinesSuccess()) {
            <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">{{ machinesSuccess() }}</div>
          }

          @if (machinesLoading()) {
            <div class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
            </div>
          } @else {
            @if (!showMachineForm() && editingMachineId() === null) {
              <div>
                <button (click)="showMachineForm.set(true)"
                  class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors">
                  + Add Machine
                </button>
              </div>
            }

            @if (showMachineForm() || editingMachineId() !== null) {
              <div class="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-3">
                <h3 class="text-sm font-semibold text-gray-700">{{ editingMachineId() !== null ? 'Edit Machine' : 'Add Machine' }}</h3>
                <form [formGroup]="machineForm" (ngSubmit)="submitMachine()" class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div class="sm:col-span-2">
                    <label class="block text-xs font-medium text-gray-600 mb-1">Name *</label>
                    <input type="text" formControlName="name" maxlength="80"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2 flex gap-2">
                    <button type="submit" [disabled]="machineSubmitting() || machineForm.invalid"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                      @if (machineSubmitting()) { Saving… } @else { {{ editingMachineId() !== null ? 'Update' : 'Save' }} }
                    </button>
                    <button type="button" (click)="cancelMachineForm()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            }

            <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Name</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (item of machines(); track item.id) {
                    <tr class="hover:bg-gray-50">
                      <td class="px-4 py-3 text-sm text-gray-900">{{ item.name }}</td>
                      <td class="px-4 py-3">
                        <div class="flex gap-2">
                          <button (click)="startEditMachine(item)"
                            class="text-xs rounded px-2 py-1 bg-yellow-50 text-yellow-700 hover:bg-yellow-100 border border-yellow-200 transition-colors">Edit</button>
                          <button (click)="deleteMachine(item.id)"
                            class="text-xs rounded px-2 py-1 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition-colors">Delete</button>
                        </div>
                      </td>
                    </tr>
                  }
                  @if (machines().length === 0) {
                    <tr><td colspan="2" class="px-4 py-8 text-center text-sm text-gray-400">No machines found.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }

      <!-- ===== SHIFTS TAB ===== -->
      @if (activeTab() === 'shifts') {
        <div class="space-y-4">
          @if (shiftsError()) {
            <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ shiftsError() }}</div>
          }
          @if (shiftsSuccess()) {
            <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">{{ shiftsSuccess() }}</div>
          }

          @if (shiftsLoading()) {
            <div class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
            </div>
          } @else {
            @if (!showShiftForm() && editingShiftId() === null) {
              <div>
                <button (click)="showShiftForm.set(true)"
                  class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors">
                  + Add Shift
                </button>
              </div>
            }

            @if (showShiftForm() || editingShiftId() !== null) {
              <div class="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-3">
                <h3 class="text-sm font-semibold text-gray-700">{{ editingShiftId() !== null ? 'Edit Shift' : 'Add Shift' }}</h3>
                <form [formGroup]="shiftForm" (ngSubmit)="submitShift()" class="grid grid-cols-1 gap-3 sm:grid-cols-3">
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">Name *</label>
                    <input type="text" formControlName="name" maxlength="90"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">Start Time *</label>
                    <input type="time" formControlName="startTime"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">End Time *</label>
                    <input type="time" formControlName="endTime"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-3 flex gap-2">
                    <button type="submit" [disabled]="shiftSubmitting() || shiftForm.invalid"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                      @if (shiftSubmitting()) { Saving… } @else { {{ editingShiftId() !== null ? 'Update' : 'Save' }} }
                    </button>
                    <button type="button" (click)="cancelShiftForm()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            }

            <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Name</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Start</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">End</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (item of shifts(); track item.id) {
                    <tr class="hover:bg-gray-50">
                      <td class="px-4 py-3 text-sm text-gray-900">{{ item.name }}</td>
                      <td class="px-4 py-3 text-sm text-gray-600">{{ item.startTime }}</td>
                      <td class="px-4 py-3 text-sm text-gray-600">{{ item.endTime }}</td>
                      <td class="px-4 py-3">
                        <div class="flex gap-2">
                          <button (click)="startEditShift(item)"
                            class="text-xs rounded px-2 py-1 bg-yellow-50 text-yellow-700 hover:bg-yellow-100 border border-yellow-200 transition-colors">Edit</button>
                          <button (click)="deleteShift(item.id)"
                            class="text-xs rounded px-2 py-1 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition-colors">Delete</button>
                        </div>
                      </td>
                    </tr>
                  }
                  @if (shifts().length === 0) {
                    <tr><td colspan="4" class="px-4 py-8 text-center text-sm text-gray-400">No shifts found.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }

      <!-- ===== CONTAINER TYPES TAB ===== -->
      @if (activeTab() === 'containerTypes') {
        <div class="space-y-4">
          @if (containerTypesError()) {
            <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ containerTypesError() }}</div>
          }
          @if (containerTypesSuccess()) {
            <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">{{ containerTypesSuccess() }}</div>
          }

          @if (containerTypesLoading()) {
            <div class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
            </div>
          } @else {
            @if (!showContainerTypeForm() && editingContainerTypeId() === null) {
              <div>
                <button (click)="showContainerTypeForm.set(true)"
                  class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors">
                  + Add Container Type
                </button>
              </div>
            }

            @if (showContainerTypeForm() || editingContainerTypeId() !== null) {
              <div class="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-3">
                <h3 class="text-sm font-semibold text-gray-700">{{ editingContainerTypeId() !== null ? 'Edit Container Type' : 'Add Container Type' }}</h3>
                <form [formGroup]="containerTypeForm" (ngSubmit)="submitContainerType()" class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div class="sm:col-span-2">
                    <label class="block text-xs font-medium text-gray-600 mb-1">Name *</label>
                    <input type="text" formControlName="name" maxlength="80"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2 flex gap-2">
                    <button type="submit" [disabled]="containerTypeSubmitting() || containerTypeForm.invalid"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                      @if (containerTypeSubmitting()) { Saving… } @else { {{ editingContainerTypeId() !== null ? 'Update' : 'Save' }} }
                    </button>
                    <button type="button" (click)="cancelContainerTypeForm()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            }

            <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Name</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (item of containerTypes(); track item.id) {
                    <tr class="hover:bg-gray-50">
                      <td class="px-4 py-3 text-sm text-gray-900">{{ item.name }}</td>
                      <td class="px-4 py-3">
                        <div class="flex gap-2">
                          <button (click)="startEditContainerType(item)"
                            class="text-xs rounded px-2 py-1 bg-yellow-50 text-yellow-700 hover:bg-yellow-100 border border-yellow-200 transition-colors">Edit</button>
                          <button (click)="deleteContainerType(item.id)"
                            class="text-xs rounded px-2 py-1 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition-colors">Delete</button>
                        </div>
                      </td>
                    </tr>
                  }
                  @if (containerTypes().length === 0) {
                    <tr><td colspan="2" class="px-4 py-8 text-center text-sm text-gray-400">No container types found.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }

      <!-- ===== CONTAINERS TAB ===== -->
      @if (activeTab() === 'containers') {
        <div class="space-y-4">
          @if (containersError()) {
            <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">{{ containersError() }}</div>
          }
          @if (containersSuccess()) {
            <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-800">{{ containersSuccess() }}</div>
          }

          @if (containersLoading()) {
            <div class="flex justify-center py-12">
              <div class="h-8 w-8 animate-spin rounded-full border-4 border-indigo-600 border-t-transparent"></div>
            </div>
          } @else {
            @if (!showContainerForm() && editingContainerId() === null) {
              <div>
                <button (click)="showContainerForm.set(true)"
                  class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors">
                  + Add Container
                </button>
              </div>
            }

            @if (showContainerForm() || editingContainerId() !== null) {
              <div class="bg-gray-50 rounded-xl border border-gray-200 p-4 space-y-3">
                <h3 class="text-sm font-semibold text-gray-700">{{ editingContainerId() !== null ? 'Edit Container' : 'Add Container' }}</h3>
                <form [formGroup]="containerForm" (ngSubmit)="submitContainer()" class="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">Container Type *</label>
                    <select formControlName="containerTypeId"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white">
                      <option [ngValue]="null" disabled>Select a container type</option>
                      @for (ct of containerTypes(); track ct.id) {
                        <option [ngValue]="ct.id">{{ ct.name }}</option>
                      }
                    </select>
                  </div>
                  <div>
                    <label class="block text-xs font-medium text-gray-600 mb-1">Code *</label>
                    <input type="text" formControlName="code" maxlength="80"
                      class="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
                  </div>
                  <div class="sm:col-span-2 flex gap-2">
                    <button type="submit" [disabled]="containerSubmitting() || containerForm.invalid"
                      class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50 transition-colors">
                      @if (containerSubmitting()) { Saving… } @else { {{ editingContainerId() !== null ? 'Update' : 'Save' }} }
                    </button>
                    <button type="button" (click)="cancelContainerForm()"
                      class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors">
                      Cancel
                    </button>
                  </div>
                </form>
              </div>
            }

            <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Code</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Container Type</th>
                    <th class="text-left text-xs font-medium text-gray-500 uppercase tracking-wider px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (item of containers(); track item.id) {
                    <tr class="hover:bg-gray-50">
                      <td class="px-4 py-3 text-sm font-medium text-gray-900">{{ item.code }}</td>
                      <td class="px-4 py-3 text-sm text-gray-600">{{ containerTypes().find(ct => ct.id === item.containerTypeId)?.name ?? item.containerTypeId }}</td>
                      <td class="px-4 py-3">
                        <div class="flex gap-2">
                          <button (click)="startEditContainer(item)"
                            class="text-xs rounded px-2 py-1 bg-yellow-50 text-yellow-700 hover:bg-yellow-100 border border-yellow-200 transition-colors">Edit</button>
                          <button (click)="deleteContainer(item.id)"
                            class="text-xs rounded px-2 py-1 bg-red-50 text-red-700 hover:bg-red-100 border border-red-200 transition-colors">Delete</button>
                        </div>
                      </td>
                    </tr>
                  }
                  @if (containers().length === 0) {
                    <tr><td colspan="3" class="px-4 py-8 text-center text-sm text-gray-400">No containers found.</td></tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class CatalogsPageComponent implements OnInit {
  private readonly api = inject(CatalogsApiClient);
  private readonly fb = inject(FormBuilder);

  readonly tabs: { key: ActiveTab; label: string }[] = [
    { key: 'profiles', label: 'Profiles' },
    { key: 'machines', label: 'Machines' },
    { key: 'shifts', label: 'Shifts' },
    { key: 'containerTypes', label: 'Container Types' },
    { key: 'containers', label: 'Containers' },
  ];

  readonly activeTab = signal<ActiveTab>('profiles');

  // Profiles state
  readonly profiles = signal<Profile[]>([]);
  readonly profilesLoading = signal(true);
  readonly profilesError = signal<string | null>(null);
  readonly profilesSuccess = signal<string | null>(null);
  readonly showProfileForm = signal(false);
  readonly editingProfileId = signal<number | null>(null);
  readonly profileSubmitting = signal(false);

  // Machines state
  readonly machines = signal<Machine[]>([]);
  readonly machinesLoading = signal(true);
  readonly machinesError = signal<string | null>(null);
  readonly machinesSuccess = signal<string | null>(null);
  readonly showMachineForm = signal(false);
  readonly editingMachineId = signal<number | null>(null);
  readonly machineSubmitting = signal(false);

  // Shifts state
  readonly shifts = signal<Shift[]>([]);
  readonly shiftsLoading = signal(true);
  readonly shiftsError = signal<string | null>(null);
  readonly shiftsSuccess = signal<string | null>(null);
  readonly showShiftForm = signal(false);
  readonly editingShiftId = signal<number | null>(null);
  readonly shiftSubmitting = signal(false);

  // Container Types state
  readonly containerTypes = signal<ContainerType[]>([]);
  readonly containerTypesLoading = signal(true);
  readonly containerTypesError = signal<string | null>(null);
  readonly containerTypesSuccess = signal<string | null>(null);
  readonly showContainerTypeForm = signal(false);
  readonly editingContainerTypeId = signal<number | null>(null);
  readonly containerTypeSubmitting = signal(false);

  // Containers state
  readonly containers = signal<CatalogContainer[]>([]);
  readonly containersLoading = signal(true);
  readonly containersError = signal<string | null>(null);
  readonly containersSuccess = signal<string | null>(null);
  readonly showContainerForm = signal(false);
  readonly editingContainerId = signal<number | null>(null);
  readonly containerSubmitting = signal(false);

  // Forms
  readonly profileCreateForm = this.fb.group({
    code: ['', [Validators.required, Validators.maxLength(10)]],
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(255)],
  });

  readonly profileEditForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', Validators.maxLength(255)],
  });

  readonly machineForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(80)]],
  });

  readonly shiftForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(90)]],
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
  });

  readonly containerTypeForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(80)]],
  });

  readonly containerForm = this.fb.group({
    containerTypeId: [null as number | null, Validators.required],
    code: ['', [Validators.required, Validators.maxLength(80)]],
  });

  ngOnInit(): void {
    forkJoin({
      profiles: this.api.getProfiles(),
      machines: this.api.getMachines(),
      shifts: this.api.getShifts(),
      containerTypes: this.api.getContainerTypes(),
      containers: this.api.getContainers(),
    }).subscribe({
      next: ({ profiles, machines, shifts, containerTypes, containers }) => {
        this.profiles.set(profiles);
        this.machines.set(machines);
        this.shifts.set(shifts);
        this.containerTypes.set(containerTypes);
        this.containers.set(containers);
        this.profilesLoading.set(false);
        this.machinesLoading.set(false);
        this.shiftsLoading.set(false);
        this.containerTypesLoading.set(false);
        this.containersLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        const msg = 'Failed to load catalog data. Please refresh.';
        this.profilesError.set(msg);
        this.machinesError.set(msg);
        this.shiftsError.set(msg);
        this.containerTypesError.set(msg);
        this.containersError.set(msg);
        this.profilesLoading.set(false);
        this.machinesLoading.set(false);
        this.shiftsLoading.set(false);
        this.containerTypesLoading.set(false);
        this.containersLoading.set(false);
      },
    });
  }

  private showSuccess(setter: (v: string | null) => void, msg: string): void {
    setter(msg);
    setTimeout(() => setter(null), 3000);
  }

  // ===== PROFILES =====

  cancelProfileForm(): void {
    this.showProfileForm.set(false);
    this.editingProfileId.set(null);
    this.profileCreateForm.reset();
    this.profileEditForm.reset();
  }

  startEditProfile(item: Profile): void {
    this.showProfileForm.set(false);
    this.editingProfileId.set(item.id);
    this.profileEditForm.setValue({ name: item.name, description: item.description ?? '' });
  }

  submitProfileCreate(): void {
    if (this.profileCreateForm.invalid || this.profileSubmitting()) return;
    this.profileSubmitting.set(true);
    this.profilesError.set(null);
    const { code, name, description } = this.profileCreateForm.getRawValue();
    this.api.createProfile({ code: code!, name: name!, description: description ?? undefined }).subscribe({
      next: (created) => {
        this.profiles.update((list) => [...list, created]);
        this.profileSubmitting.set(false);
        this.showProfileForm.set(false);
        this.profileCreateForm.reset();
        this.showSuccess((v) => this.profilesSuccess.set(v), 'Profile created.');
      },
      error: () => {
        this.profilesError.set('Failed to create profile.');
        this.profileSubmitting.set(false);
      },
    });
  }

  submitProfileEdit(): void {
    if (this.profileEditForm.invalid || this.profileSubmitting() || this.editingProfileId() === null) return;
    this.profileSubmitting.set(true);
    this.profilesError.set(null);
    const { name, description } = this.profileEditForm.getRawValue();
    this.api.updateProfile(this.editingProfileId()!, { name: name!, description: description ?? undefined }).subscribe({
      next: (updated) => {
        this.profiles.update((list) => list.map((p) => (p.id === updated.id ? updated : p)));
        this.profileSubmitting.set(false);
        this.editingProfileId.set(null);
        this.profileEditForm.reset();
        this.showSuccess((v) => this.profilesSuccess.set(v), 'Profile updated.');
      },
      error: () => {
        this.profilesError.set('Failed to update profile.');
        this.profileSubmitting.set(false);
      },
    });
  }

  deleteProfile(id: number): void {
    if (!window.confirm('Delete this profile?')) return;
    this.api.deleteProfile(id).subscribe({
      next: () => {
        this.profiles.update((list) => list.filter((p) => p.id !== id));
        this.showSuccess((v) => this.profilesSuccess.set(v), 'Profile deleted.');
      },
      error: () => this.profilesError.set('Failed to delete profile.'),
    });
  }

  // ===== MACHINES =====

  cancelMachineForm(): void {
    this.showMachineForm.set(false);
    this.editingMachineId.set(null);
    this.machineForm.reset();
  }

  startEditMachine(item: Machine): void {
    this.showMachineForm.set(false);
    this.editingMachineId.set(item.id);
    this.machineForm.setValue({ name: item.name });
  }

  submitMachine(): void {
    if (this.machineForm.invalid || this.machineSubmitting()) return;
    this.machineSubmitting.set(true);
    this.machinesError.set(null);
    const { name } = this.machineForm.getRawValue();
    const req = this.editingMachineId() !== null
      ? this.api.updateMachine(this.editingMachineId()!, { name: name! })
      : this.api.createMachine({ name: name! });
    const isEdit = this.editingMachineId() !== null;
    req.subscribe({
      next: (saved) => {
        if (isEdit) {
          this.machines.update((list) => list.map((m) => (m.id === saved.id ? saved : m)));
        } else {
          this.machines.update((list) => [...list, saved]);
        }
        this.machineSubmitting.set(false);
        this.showMachineForm.set(false);
        this.editingMachineId.set(null);
        this.machineForm.reset();
        this.showSuccess((v) => this.machinesSuccess.set(v), isEdit ? 'Machine updated.' : 'Machine created.');
      },
      error: () => {
        this.machinesError.set('Failed to save machine.');
        this.machineSubmitting.set(false);
      },
    });
  }

  deleteMachine(id: number): void {
    if (!window.confirm('Delete this machine?')) return;
    this.api.deleteMachine(id).subscribe({
      next: () => {
        this.machines.update((list) => list.filter((m) => m.id !== id));
        this.showSuccess((v) => this.machinesSuccess.set(v), 'Machine deleted.');
      },
      error: () => this.machinesError.set('Failed to delete machine.'),
    });
  }

  // ===== SHIFTS =====

  cancelShiftForm(): void {
    this.showShiftForm.set(false);
    this.editingShiftId.set(null);
    this.shiftForm.reset();
  }

  startEditShift(item: Shift): void {
    this.showShiftForm.set(false);
    this.editingShiftId.set(item.id);
    this.shiftForm.setValue({ name: item.name, startTime: item.startTime, endTime: item.endTime });
  }

  submitShift(): void {
    if (this.shiftForm.invalid || this.shiftSubmitting()) return;
    this.shiftSubmitting.set(true);
    this.shiftsError.set(null);
    const { name, startTime, endTime } = this.shiftForm.getRawValue();
    const isEdit = this.editingShiftId() !== null;
    const req = isEdit
      ? this.api.updateShift(this.editingShiftId()!, { name: name!, startTime: startTime!, endTime: endTime! })
      : this.api.createShift({ name: name!, startTime: startTime!, endTime: endTime! });
    req.subscribe({
      next: (saved) => {
        if (isEdit) {
          this.shifts.update((list) => list.map((s) => (s.id === saved.id ? saved : s)));
        } else {
          this.shifts.update((list) => [...list, saved]);
        }
        this.shiftSubmitting.set(false);
        this.showShiftForm.set(false);
        this.editingShiftId.set(null);
        this.shiftForm.reset();
        this.showSuccess((v) => this.shiftsSuccess.set(v), isEdit ? 'Shift updated.' : 'Shift created.');
      },
      error: () => {
        this.shiftsError.set('Failed to save shift.');
        this.shiftSubmitting.set(false);
      },
    });
  }

  deleteShift(id: number): void {
    if (!window.confirm('Delete this shift?')) return;
    this.api.deleteShift(id).subscribe({
      next: () => {
        this.shifts.update((list) => list.filter((s) => s.id !== id));
        this.showSuccess((v) => this.shiftsSuccess.set(v), 'Shift deleted.');
      },
      error: () => this.shiftsError.set('Failed to delete shift.'),
    });
  }

  // ===== CONTAINER TYPES =====

  cancelContainerTypeForm(): void {
    this.showContainerTypeForm.set(false);
    this.editingContainerTypeId.set(null);
    this.containerTypeForm.reset();
  }

  startEditContainerType(item: ContainerType): void {
    this.showContainerTypeForm.set(false);
    this.editingContainerTypeId.set(item.id);
    this.containerTypeForm.setValue({ name: item.name });
  }

  submitContainerType(): void {
    if (this.containerTypeForm.invalid || this.containerTypeSubmitting()) return;
    this.containerTypeSubmitting.set(true);
    this.containerTypesError.set(null);
    const { name } = this.containerTypeForm.getRawValue();
    const isEdit = this.editingContainerTypeId() !== null;
    const req = isEdit
      ? this.api.updateContainerType(this.editingContainerTypeId()!, { name: name! })
      : this.api.createContainerType({ name: name! });
    req.subscribe({
      next: (saved) => {
        if (isEdit) {
          this.containerTypes.update((list) => list.map((ct) => (ct.id === saved.id ? saved : ct)));
        } else {
          this.containerTypes.update((list) => [...list, saved]);
        }
        this.containerTypeSubmitting.set(false);
        this.showContainerTypeForm.set(false);
        this.editingContainerTypeId.set(null);
        this.containerTypeForm.reset();
        this.showSuccess((v) => this.containerTypesSuccess.set(v), isEdit ? 'Container type updated.' : 'Container type created.');
      },
      error: () => {
        this.containerTypesError.set('Failed to save container type.');
        this.containerTypeSubmitting.set(false);
      },
    });
  }

  deleteContainerType(id: number): void {
    if (!window.confirm('Delete this container type?')) return;
    this.api.deleteContainerType(id).subscribe({
      next: () => {
        this.containerTypes.update((list) => list.filter((ct) => ct.id !== id));
        this.showSuccess((v) => this.containerTypesSuccess.set(v), 'Container type deleted.');
      },
      error: () => this.containerTypesError.set('Failed to delete container type.'),
    });
  }

  // ===== CONTAINERS =====

  cancelContainerForm(): void {
    this.showContainerForm.set(false);
    this.editingContainerId.set(null);
    this.containerForm.reset();
  }

  startEditContainer(item: CatalogContainer): void {
    this.showContainerForm.set(false);
    this.editingContainerId.set(item.id);
    this.containerForm.setValue({ containerTypeId: item.containerTypeId, code: item.code });
  }

  submitContainer(): void {
    if (this.containerForm.invalid || this.containerSubmitting()) return;
    this.containerSubmitting.set(true);
    this.containersError.set(null);
    const { containerTypeId, code } = this.containerForm.getRawValue();
    const isEdit = this.editingContainerId() !== null;
    const req = isEdit
      ? this.api.updateContainer(this.editingContainerId()!, { containerTypeId: containerTypeId!, code: code! })
      : this.api.createContainer({ containerTypeId: containerTypeId!, code: code! });
    req.subscribe({
      next: (saved) => {
        if (isEdit) {
          this.containers.update((list) => list.map((c) => (c.id === saved.id ? saved : c)));
        } else {
          this.containers.update((list) => [...list, saved]);
        }
        this.containerSubmitting.set(false);
        this.showContainerForm.set(false);
        this.editingContainerId.set(null);
        this.containerForm.reset();
        this.showSuccess((v) => this.containersSuccess.set(v), isEdit ? 'Container updated.' : 'Container created.');
      },
      error: () => {
        this.containersError.set('Failed to save container.');
        this.containerSubmitting.set(false);
      },
    });
  }

  deleteContainer(id: number): void {
    if (!window.confirm('Delete this container?')) return;
    this.api.deleteContainer(id).subscribe({
      next: () => {
        this.containers.update((list) => list.filter((c) => c.id !== id));
        this.showSuccess((v) => this.containersSuccess.set(v), 'Container deleted.');
      },
      error: () => this.containersError.set('Failed to delete container.'),
    });
  }
}