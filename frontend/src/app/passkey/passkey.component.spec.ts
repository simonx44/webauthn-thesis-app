import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasskeyComponent } from './passkey.component';

describe('PasskeyComponent', () => {
  let component: PasskeyComponent;
  let fixture: ComponentFixture<PasskeyComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PasskeyComponent]
    });
    fixture = TestBed.createComponent(PasskeyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
