import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RiskProfileComponent } from './risk-profile-component';

describe('RiskProfileComponent', () => {
  let component: RiskProfileComponent;
  let fixture: ComponentFixture<RiskProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RiskProfileComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RiskProfileComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
