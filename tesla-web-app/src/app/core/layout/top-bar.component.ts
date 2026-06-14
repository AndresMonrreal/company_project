import { Component, inject } from '@angular/core';
import { AuthSession } from '../auth/auth-session';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [],
  template: `
    <header class="h-14 bg-white border-b border-gray-200 flex items-center justify-end px-6 gap-6">
      <span class="text-sm text-gray-600">Shift A · 06:00–14:00</span>
      <span class="text-sm text-gray-600">Today · {{ todayFormatted }}</span>
      <button class="text-gray-500 hover:text-gray-800">🔔</button>
      <div class="w-8 h-8 rounded-full bg-gray-800 text-white text-xs font-semibold flex items-center justify-center">
        {{ authSession.initials() }}
      </div>
    </header>
  `,
})
export class TopBarComponent {
  protected readonly authSession = inject(AuthSession);

  protected readonly todayFormatted = new Date().toLocaleDateString('es-MX', {
    day: '2-digit',
    month: 'numeric',
    year: 'numeric',
  });
}
