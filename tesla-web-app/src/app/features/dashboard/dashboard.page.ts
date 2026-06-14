import { Component, inject } from '@angular/core';
import { AuthSession } from '../../core/auth/auth-session';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [],
  template: `
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
      @if (authSession.session()) {
        <p class="text-gray-600 mt-1">Welcome, {{ authSession.session()!.fullName }}</p>
      }
    </div>
  `,
})
export class DashboardPageComponent {
  protected readonly authSession = inject(AuthSession);
}
