import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Coordinador } from './coordinador';

describe('Coordinador', () => {
  let component: Coordinador;
  let fixture: ComponentFixture<Coordinador>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Coordinador]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Coordinador);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
