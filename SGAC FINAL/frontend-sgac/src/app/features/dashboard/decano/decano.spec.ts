import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Decano } from './decano';

describe('Decano', () => {
  let component: Decano;
  let fixture: ComponentFixture<Decano>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Decano]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Decano);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
