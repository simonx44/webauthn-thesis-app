import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {WebauthnService} from "../../service/webauthn.service";
import {NxDialogService, NxModalRef} from '@aposin/ng-aquila/modal';
import {FormControl, FormGroup, Validators} from "@angular/forms";


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  public formGroup;
  @ViewChild('template') templateRef!: TemplateRef<any>;
  templateDialogRef?: NxModalRef<any>;

  public httpRequest = {isLoading: false, isError: false};
  public authenticators: Array<any>;

  constructor(private authService: WebauthnService, private readonly dialogService: NxDialogService) {
    this.authenticators = [];
    this.formGroup = new FormGroup({
      passkeyName: new FormControl("", [
        Validators.required,
        Validators.minLength(4),
      ]),
    });
  }

  ngOnInit(): void {
    this.getUserData();

   // navigator.credentials.
  }

  private validateInput() {
    this.formGroup.controls['passkeyName'].markAsTouched();
  }

  public getUserData() {
    this.httpRequest = {isError: false, isLoading: true};
    this.authService.getUserAuthenticators().subscribe(res => {
      console.log(res);
      this.httpRequest = {isError: false, isLoading: false};
      this.authenticators = res;
    }, (err) => {
      this.httpRequest = {isError: true, isLoading: false};
    });

  }


  openModal(): void {

    this.formGroup.reset();
    this.templateDialogRef = this.dialogService.open(this.templateRef, {
      ariaLabel: 'A simple dialog',
    });
  }

  public isValueValid() {
    const passkeyName = this.formGroup.controls.passkeyName?.value;
    // @ts-ignore
    return passkeyName?.length > 4;
  }

  public registerPasskey() {
    const passkeyName = this.formGroup.controls.passkeyName?.value;
    this.validateInput();
    if (!this.formGroup.valid || !passkeyName) {
      return;
    }

    this.closeModal();

    this.authService.startAddCredentialCeremony(passkeyName)
      .subscribe(() => {
          console.log("success")
        },
        () => {
          console.log("errror");
        }
      )


    this.formGroup.controls['passkeyName'].setValue("");
    console.log("registerPasskey");
  }


  closeModal() {

    this.templateDialogRef?.close();
  }


}


