import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuditoriaPage } from './auditoria.page';

describe('AuditoriaPage', () => {
  let component: AuditoriaPage;
  let fixture: ComponentFixture<AuditoriaPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditoriaPage],
    }).compileComponents();

    fixture = TestBed.createComponent(AuditoriaPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
