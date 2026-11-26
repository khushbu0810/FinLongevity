import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NomineesComponent } from './nominees-component';

describe('NomineesComponent', () => {
  let component: NomineesComponent;
  let fixture: ComponentFixture<NomineesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NomineesComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NomineesComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
