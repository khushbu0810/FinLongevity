import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LiabilitiesComponent } from './liabilities-component';

describe('LiabilitiesComponent', () => {
  let component: LiabilitiesComponent;
  let fixture: ComponentFixture<LiabilitiesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LiabilitiesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(LiabilitiesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
