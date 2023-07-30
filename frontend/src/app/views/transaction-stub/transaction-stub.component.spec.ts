import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionStubComponent } from './transaction-stub.component';

describe('TransactionStubComponent', () => {
  let component: TransactionStubComponent;
  let fixture: ComponentFixture<TransactionStubComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TransactionStubComponent]
    });
    fixture = TestBed.createComponent(TransactionStubComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
