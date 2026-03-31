import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageCage } from './page-cage.component';

describe('PageCage', () => {
  let component: PageCage;
  let fixture: ComponentFixture<PageCage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageCage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PageCage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
