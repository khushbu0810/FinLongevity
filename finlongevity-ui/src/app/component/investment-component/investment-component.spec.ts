import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InvestmentComponent } from './investment-component';

describe('InvestmentComponent', () => {
  let component: InvestmentComponent;
  let fixture: ComponentFixture<InvestmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvestmentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InvestmentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
