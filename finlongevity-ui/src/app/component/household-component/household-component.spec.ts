import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HouseholdComponent } from './household-component';

describe('HouseholdComponent', () => {
  let component: HouseholdComponent;
  let fixture: ComponentFixture<HouseholdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HouseholdComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(HouseholdComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
