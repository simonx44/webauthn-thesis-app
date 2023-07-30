import {Component} from '@angular/core';
import {WebauthnService} from "../../service/webauthn.service";

@Component({
    selector: 'app-transaction-stub',
    templateUrl: './transaction-stub.component.html',
    styleUrls: ['./transaction-stub.component.scss']
})
export class TransactionStubComponent {

    public moneyToTransfer: number = 50;

    public response : {transactionAuthorized: boolean} | undefined = undefined;


    constructor(private authService: WebauthnService) {

    }

    public confirmTransaction() {

        this.authService.confirmTransaction(this.moneyToTransfer)
            .subscribe(() => {
                this.response = {transactionAuthorized:  true};
            }, () => {
                this.response = {transactionAuthorized:  false};
            })

    }

}
