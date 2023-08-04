import {Component, OnInit} from '@angular/core';
import {WebauthnService} from "../../service/webauthn.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {NxMessageToastConfig, NxMessageToastRef, NxMessageToastService} from "@aposin/ng-aquila/message";
import {CompletionApiResult} from "../../types";
import {Router} from "@angular/router";
import {UserService} from "../../service/user.service";
import AbortError from "../../errors/AbortError";


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

  public abortController: AbortController | undefined;

  public formGroup;
  public autoFillMode: boolean = true;
  public usernamelessFlow: { fetching: boolean, options?: CredentialRequestOptions } = {
    fetching: false,
    options: undefined
  };
  public isLoading: boolean = false;
  public webAuthNSupported = true;
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
      ],),
    }, {updateOn: 'submit'});
  }

  ngOnInit(): void {

    this.initComponent();

  }

  async mediationAvailable() {

    console.log(window.PublicKeyCredential);

    if(!window.PublicKeyCredential){
      this.webAuthNSupported = false;
      return false;
    }

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
      this.initUsernamelessFlow();
    } else {
      this.autoFillMode = false;
    }

  }

  private validateInput() {
    this.formGroup.controls['username'].markAsTouched();
  }

  public initUsernamelessFlow() {
    this.createAbortController();
    this.authService.initAutofillAuthentication().subscribe((res) => {
      console.log("rest")
      this.onSuccess(res)
    }, (err) => {
      this.handleError(err);
    })
  }

  private createAbortController() {
    this.abortController = new AbortController();
    const authAbortSignal = this.abortController.signal;
    authAbortSignal.onabort = () => {
      console.log("Abort");
    };
    this.authService.abortSignal = authAbortSignal;

  }


  private abortAutofillCeremony() {
    this.abortController?.abort(new AbortError("Abort prev request"));
  }

  public usernamelessLogin() {
    this.abortAutofillCeremony();
    this.authService.authenticateUsernameLess().subscribe((res) => {
      this.onSuccess(res)
    }, (err) => {
      this.handleError(err)
    })
  }


  private handleError(error: Error) {

    if (error instanceof AbortError) {
      console.log("Request was aborted")
      return;
    } else if (error instanceof HttpErrorResponse) {
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
    this.initUsernamelessFlow();
  }

  public initAuth() {
    this.error.isError = false;
    this.clearError();
    this.isLoginMode ? this.initLogin() : this.initRegistration();
  }

  public clearError(){
    console.log("Updae")
    this.error = {isError: false, msg: ""};
    this.formGroup.controls.username.markAsUntouched();

  }

  private initLogin() {
    const username = this.formGroup.controls.username?.value;
    console.log( this.formGroup.controls);
    console.log("username", username)
    this.validateInput();
    if (!this.formGroup.valid || !username) {
      return;
    }
    this.abortAutofillCeremony();
    this.authService.authenticateUser(username)
      .subscribe(res => this.onSuccess(res), (err) => this.handleError(err), () => {
      });
  }

  private initRegistration() {
    this.abortAutofillCeremony();
    const username = this.formGroup.controls.username?.value;
    //   this.validateInput();
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
