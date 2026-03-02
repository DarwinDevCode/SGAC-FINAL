import { TestBed } from '@angular/core/testing';

import { Alo } from './alo';

describe('Alo', () => {
  let service: Alo;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Alo);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
