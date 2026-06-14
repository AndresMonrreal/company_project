import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-coming-soon',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="flex flex-col items-center justify-center h-full text-center py-24">
      <p class="text-2xl font-semibold text-gray-700">This feature is coming soon.</p>
      <a routerLink="/dashboard" class="mt-4 text-indigo-600 hover:underline text-sm">← Back to Dashboard</a>
    </div>
  `,
})
export class ComingSoonComponent {}
