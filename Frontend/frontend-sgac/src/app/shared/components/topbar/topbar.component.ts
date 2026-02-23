import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth-service';
import { LucideAngularModule, Bell } from 'lucide-angular';

@Component({
    selector: 'app-topbar',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    template: `
    <header class="bg-white shadow-sm h-16 flex items-center justify-between px-6 sticky top-0 z-20">
      <!-- Left: Title or Breadcrumbs (Placeholder) -->
      <div>
        <h1 class="text-xl font-semibold text-gray-800">Panel de Control</h1>
      </div>

      <!-- Right: Notifications & Profile -->
      <div class="flex items-center space-x-4">
        <button class="p-2 rounded-full text-gray-500 hover:bg-gray-100 relative">
          <lucide-icon name="bell" class="h-5 w-5"></lucide-icon>
          <span class="absolute top-1.5 right-1.5 h-2 w-2 bg-red-500 rounded-full"></span>
        </button>

        <div class="flex items-center space-x-3 border-l pl-4 border-gray-200">
           <div class="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-bold text-sm">
             {{ user?.nombres?.charAt(0) }}{{ user?.apellidos?.charAt(0) }}
           </div>
           <span class="text-sm font-medium text-gray-700 hidden sm:block">{{ user?.nombres }}</span>
        </div>
      </div>
    </header>
  `
})
export class TopbarComponent {
    private authService = inject(AuthService);
    user = this.authService.getCurrentUser();

    readonly Bell = Bell;
}
