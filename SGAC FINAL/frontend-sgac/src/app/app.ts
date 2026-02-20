import { Component, signal, ViewEncapsulation } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`,
  styles: [],
  encapsulation: ViewEncapsulation.None
})
export class App {
  protected readonly title = signal('frontend-sgac');
}
