import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FinancialEngineComponent } from './financial-engine-component';

describe('FinancialEngineComponent', () => {
  let component: FinancialEngineComponent;
  let fixture: ComponentFixture<FinancialEngineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinancialEngineComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FinancialEngineComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
