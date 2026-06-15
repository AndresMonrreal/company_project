import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthSession } from '../auth/auth-session';
import { AuthService } from '../auth/auth.service';
import { NAV_ITEMS, NavItem } from './nav-item.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside class="fixed left-0 top-0 h-screen w-64 bg-gray-900 flex flex-col text-white z-50">
      <div class="px-4 pt-6 pb-4 flex flex-col">
        <span class="font-bold text-xl">RubberTrace</span>
        <span class="text-xs text-gray-400 tracking-widest">OPERATIONS</span>
        <span class="text-xs uppercase bg-gray-700 rounded px-2 py-0.5 mt-1 self-start">
          {{ authSession.role() }}
        </span>
      </div>

      <nav class="flex-1 mt-6 overflow-y-auto">
        @for (item of visibleNavItems; track item.route + item.label) {
          <a
            [routerLink]="item.route"
            routerLinkActive="bg-gray-700"
            class="flex items-center gap-3 px-4 py-3 text-gray-300 hover:bg-gray-700 hover:text-white transition-colors rounded-lg mx-2 my-0.5"
          >{{ item.label }}</a>
        }
      </nav>

      <div class="p-4 border-t border-gray-700">
        <button (click)="showLogoutModal.set(true)" class="w-full text-left text-gray-400 hover:text-white text-sm py-2">
          Sign out
        </button>
      </div>

      @if (showLogoutModal()) {
        <div class="fixed inset-0 z-[100] flex items-center justify-center bg-black/60">
          <div class="bg-white rounded-2xl shadow-2xl p-6 w-80">
            <h2 class="text-lg font-bold text-gray-900 mb-2">Sign out</h2>
            <p class="text-sm text-gray-600 mb-6">Are you sure you want to sign out?</p>
            <div class="flex gap-3 justify-end">
              <button
                type="button"
                (click)="showLogoutModal.set(false)"
                class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
              >Cancel</button>
              <button
                type="button"
                (click)="confirmLogout()"
                class="px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-500 rounded-lg transition-colors"
              >Sign out</button>
            </div>
          </div>
        </div>
      }
    </aside>
  `,
})
export class SidebarComponent {
  protected readonly authSession = inject(AuthSession);
  protected readonly authService = inject(AuthService);
  protected readonly showLogoutModal = signal(false);

  protected get visibleNavItems(): NavItem[] {
    const role = this.authSession.role();
    if (role === null) return [];
    return NAV_ITEMS.filter((item) => item.roles.includes(role));
  }

  protected confirmLogout(): void {
    this.authService.logout();
  }
}
