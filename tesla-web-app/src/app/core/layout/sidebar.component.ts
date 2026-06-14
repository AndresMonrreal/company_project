import { Component, inject } from '@angular/core';
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
        <button (click)="logout()" class="w-full text-left text-gray-400 hover:text-white text-sm py-2">
          Sign out
        </button>
      </div>
    </aside>
  `,
})
export class SidebarComponent {
  protected readonly authSession = inject(AuthSession);
  protected readonly authService = inject(AuthService);

  protected get visibleNavItems(): NavItem[] {
    const role = this.authSession.role();
    if (role === null) return [];
    return NAV_ITEMS.filter((item) => item.roles.includes(role));
  }

  protected logout(): void {
    this.authService.logout();
  }
}
