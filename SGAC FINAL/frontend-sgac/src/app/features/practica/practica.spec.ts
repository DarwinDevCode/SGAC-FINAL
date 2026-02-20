import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Practica } from './practica';

describe('Practica', () => {
  let component: Practica;
  let fixture: ComponentFixture<Practica>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Practica]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Practica);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
