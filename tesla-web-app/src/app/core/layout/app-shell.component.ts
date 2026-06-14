import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { TopBarComponent } from './top-bar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [SidebarComponent, TopBarComponent, RouterOutlet],
  template: `
    <div class="flex h-screen overflow-hidden">
      <app-sidebar />
      <div class="flex flex-col flex-1 ml-64 overflow-hidden">
        <app-top-bar />
        <main class="flex-1 overflow-y-auto bg-gray-50 p-6">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class AppShellComponent {}
