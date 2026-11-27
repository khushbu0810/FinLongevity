import { TestBed } from '@angular/core/testing';
import { FinancialEngineService } from './financial-engine-service';

describe('FinancialEngineService', () => {
  let service: FinancialEngineService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FinancialEngineService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
