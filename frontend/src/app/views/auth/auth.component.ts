import {Component, OnInit} from '@angular/core';
import {WebauthnService} from "../../service/webauthn.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {NxMessageToastConfig, NxMessageToastRef, NxMessageToastService} from "@aposin/ng-aquila/message";
import {CompletionApiResult} from "../../types";
import {Router} from "@angular/router";
import {UserService} from "../../service/user.service";

const config: NxMessageToastConfig = {
    duration: 2500,
    context: 'success',
    announcementMessage: "Info",
    politeness: "polite"
}

@Component({
    selector: 'app-auth',
    templateUrl: './auth.component.html',
    styleUrls: ['./auth.component.scss']
})
export class AuthComponent implements OnInit {

    public formGroup;
    public autoFillMode: boolean = true;
    public isLoading: boolean = false;
    public isLoginMode = true;
    public error = {isError: false, msg: ""};
    toastRef!: NxMessageToastRef;

    constructor(private authService: WebauthnService,
                private readonly messageToastService: NxMessageToastService,
                private router: Router
        , private userService: UserService) {
        this.formGroup = new FormGroup({
            username: new FormControl("", [
                Validators.required,
                Validators.minLength(4),
            ]),
        });
    }

    ngOnInit(): void {

        this.initComponent();

    }

    async mediationAvailable() {
        const pubKeyCred = PublicKeyCredential;
        // Check if the function exists on the browser - Not safe to assume as the page will crash if the function is not available
        //typeof check is used as browsers that do not support mediation will not have the 'isConditionalMediationAvailable' method available
        if (
            typeof pubKeyCred.isConditionalMediationAvailable === "function" &&
            await pubKeyCred.isConditionalMediationAvailable()
        ) {
            console.log("Conditional Mediation is available");
            return true;
        }
        console.log("Conditional Mediation is not available");
        return false;
    };


    private async initComponent() {
        if (await this.mediationAvailable()) {
            this.autoFillMode = true;
        } else {
            this.autoFillMode = false;
        }


    }

    private validateInput() {
        this.formGroup.controls['username'].markAsTouched();
    }


    private handleError(error: HttpErrorResponse) {

        console.log(error)
        if (error.status === 0) {
            // A client-side or network error occurred. Handle it accordingly.
            this.error = {isError: true, msg: error.message};
        } else {
            console.log(error)
            // The backend returned an unsuccessful response code.
            console.log(
                `Backend returned code ${error.status}, body was: `, error.error);

            this.error = {isError: true, msg: error.message};

        }

    }

    public initAuth() {
        this.error.isError = false;
        this.isLoginMode ? this.initLogin() : this.initRegistration();
    }

    private initLogin() {
        console.log("LOGIN INIT:")
        const username = this.formGroup.controls.username?.value;
        this.validateInput();
        if (!this.formGroup.valid || !username) {
            return;
        }

        this.authService.authenticateUser(username)
            .subscribe(res => this.onSuccess(res), (err) => this.handleError(err), () => {
            });
    }

    private initRegistration() {
        const username = this.formGroup.controls.username?.value;
        this.validateInput();
        console.log("validate");
        if (!this.formGroup.valid || !username) {
            return;
        }
        this.authService.startRegisterCeremony(username)
            .subscribe((opt) => this.onSuccess(opt),
                (err) => this.handleError(err),
                () => {
                });

    }

    public onSuccess(options: CompletionApiResult) {

        this.userService.isUserLoggedIn = true;
        this.router.navigateByUrl("/home");
    }

    public handleModeSwitch() {
        this.isLoginMode = !this.isLoginMode;
        this.error.isError = false;
        this.userService.isUserLoggedIn = false;
    }


    get username() {
        return this.formGroup.get('username');
    }


}
