import { TestBed } from '@angular/core/testing';
import { InvestmentDraftService } from './investment-draft-service';

describe('InvestmentDraftService', () => {
  let service: InvestmentDraftService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InvestmentDraftService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
