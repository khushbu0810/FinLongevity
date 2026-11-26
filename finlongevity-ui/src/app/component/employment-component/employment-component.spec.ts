import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmploymentComponent } from './employment-component';

describe('EmploymentComponent', () => {
  let component: EmploymentComponent;
  let fixture: ComponentFixture<EmploymentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmploymentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EmploymentComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
