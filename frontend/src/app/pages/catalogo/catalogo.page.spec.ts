import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CatalogoPage } from './catalogo.page';

describe('CatalogoPage', () => {
  let component: CatalogoPage;
  let fixture: ComponentFixture<CatalogoPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CatalogoPage],
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogoPage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
