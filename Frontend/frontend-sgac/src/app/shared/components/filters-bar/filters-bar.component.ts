import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Search, Filter, X } from 'lucide-angular';

@Component({
    selector: 'app-filters-bar',
    standalone: true,
    imports: [CommonModule, FormsModule, LucideAngularModule],
    template: `
    <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200 flex flex-col sm:flex-row gap-4 items-center justify-between">

      <!-- Search Input -->
      <div class="relative flex-1 w-full">
        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <lucide-icon name="search" class="h-5 w-5 text-gray-400"></lucide-icon>
        </div>
        <input
          type="text"
          [(ngModel)]="searchQuery"
          (ngModelChange)="onSearchChange()"
          class="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
          [placeholder]="placeholder">
        <div *ngIf="searchQuery" class="absolute inset-y-0 right-0 pr-3 flex items-center cursor-pointer" (click)="clearSearch()">
            <lucide-icon name="x" class="h-4 w-4 text-gray-400 hover:text-gray-600"></lucide-icon>
        </div>
      </div>

      <div class="flex items-center gap-2 w-full sm:w-auto">
        <ng-content></ng-content>
      </div>
    </div>
  `
})
export class FiltersBarComponent {
    @Input() placeholder = 'Buscar...';
    @Output() searchChange = new EventEmitter<string>();

    searchQuery = '';

    readonly Search = Search;
    readonly Filter = Filter;
    readonly X = X;

    onSearchChange() {
        this.searchChange.emit(this.searchQuery);
    }

    clearSearch() {
        this.searchQuery = '';
        this.onSearchChange();
    }
}
