import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthCreateResultParserComponent } from './auth-create-result-parser.component';

describe('AuthCreateResultParserComponent', () => {
  let component: AuthCreateResultParserComponent;
  let fixture: ComponentFixture<AuthCreateResultParserComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AuthCreateResultParserComponent]
    });
    fixture = TestBed.createComponent(AuthCreateResultParserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
