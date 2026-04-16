import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { PageCage } from './page-cage.component';

describe('PageCage', () => {
  let component: PageCage;
  let fixture: ComponentFixture<PageCage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageCage],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
              queryParamMap: convertToParamMap({}),
            },
            params: of({}),
            queryParams: of({}),
            fragment: of(null),
            data: of({}),
            url: of([]),
          },
        },
      ],
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
