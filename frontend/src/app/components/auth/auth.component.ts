import {Component, TemplateRef, ViewChild} from '@angular/core';
import {WebauthnService} from "../../service/webauthn.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {NxMessageToastConfig, NxMessageToastRef, NxMessageToastService} from "@aposin/ng-aquila/message";
import {CompletionApiResult} from "../../types";
import {Router} from "@angular/router";

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
export class AuthComponent {

  public formGroup;
  public isLoading: boolean = false;
  public error = {isError: false, msg: ""};
  toastRef!: NxMessageToastRef;

  constructor(private authService: WebauthnService,
              private readonly messageToastService: NxMessageToastService,
              private router: Router) {
    this.formGroup = new FormGroup({
      username: new FormControl("", [
        Validators.required,
        Validators.minLength(4),
      ]),
    });
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

    this.toastRef = this.messageToastService.open(
      "Operation failed", {
        ...config,
        // @ts-ignore
        context: "error"
      }
    );

  }

  public initLogin() {
    const username = this.formGroup.controls.username?.value;
    this.validateInput();
    if (!this.formGroup.valid || !username) {
      return;
    }
    this.authService.authenticateUser(username)
      .subscribe(res => this.onSuccess(res), (err) => this.handleError(err), () => {
      });
  }

  public initRegistration() {
    const username = this.formGroup.controls.username?.value;
    this.validateInput();
    console.log("validate");
    if (!this.formGroup.valid || !username) {
      return;
    }
    this.authService.startRegisterCeremony(username)
      .subscribe(this.onSuccess, this.handleError, () => {
      });

  }

  public onSuccess(options: CompletionApiResult) {
    this.toastRef = this.messageToastService.open(
      options?.msg ?? "Success", config
    );
    this.router.navigateByUrl("/home");
  }


  get username() {
    return this.formGroup.get('username');
  }


}
