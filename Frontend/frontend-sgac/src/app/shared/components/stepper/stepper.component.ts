import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Check } from 'lucide-angular';

@Component({
    selector: 'app-stepper',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    template: `
    <nav aria-label="Progress">
      <ol role="list" class="flex items-center">
        <li *ngFor="let step of steps; let i = index; let last = last" [class.w-full]="!last" class="relative">
          
          <div class="flex items-center" [class.pr-4]="!last">
            <!-- Completed Step -->
            <div *ngIf="i < currentStep" class="flex-shrink-0 relative">
               <div class="w-8 h-8 flex items-center justify-center bg-primary-600 rounded-full hover:bg-primary-900 transition-colors">
                  <lucide-icon name="check" class="w-5 h-5 text-white"></lucide-icon>
               </div>
               <span class="absolute -bottom-6 left-1/2 transform -translate-x-1/2 text-xs font-medium text-primary-600 w-max">{{ step }}</span>
            </div>

            <!-- Current Step -->
            <div *ngIf="i === currentStep" class="flex-shrink-0 relative">
               <div class="w-8 h-8 flex items-center justify-center border-2 border-primary-600 bg-white rounded-full" aria-current="step">
                  <span class="text-primary-600 font-bold">{{ i + 1 }}</span>
               </div>
               <span class="absolute -bottom-6 left-1/2 transform -translate-x-1/2 text-xs font-bold text-primary-600 w-max">{{ step }}</span>
            </div>

            <!-- Upcoming Step -->
            <div *ngIf="i > currentStep" class="flex-shrink-0 relative">
               <div class="w-8 h-8 flex items-center justify-center border-2 border-gray-300 bg-white rounded-full hover:border-gray-400">
                  <span class="text-gray-500">{{ i + 1 }}</span>
               </div>
               <span class="absolute -bottom-6 left-1/2 transform -translate-x-1/2 text-xs font-medium text-gray-500 w-max">{{ step }}</span>
            </div>

            <!-- Connector Line -->
             <div *ngIf="!last" class="hidden sm:block absolute top-4 left-0 w-full h-0.5" aria-hidden="true"
                 [class.bg-primary-600]="i < currentStep"
                 [class.bg-gray-200]="i >= currentStep"
                 style="left: 2rem; width: calc(100% - 4rem);">
             </div>
          </div>
        </li>
      </ol>
    </nav>
  `
})
export class StepperComponent {
    @Input({ required: true }) steps: string[] = [];
    @Input({ required: true }) currentStep: number = 0; // 0-indexed

    readonly Check = Check;
}
