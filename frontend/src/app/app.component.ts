import { Component, TemplateRef, ViewChild } from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import { NxDialogService, NxModalRef } from '@aposin/ng-aquila/modal';
import {WebauthnService} from "./service/webauthn.service";
import {HttpErrorResponse} from "@angular/common/http";
import {catchError, throwError} from "rxjs";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
})
export class AppComponent {
    public formGroup;
    public isLoading: boolean = false;
    public error = {isError: false, msg: ""};

    constructor(private authService: WebauthnService) {
      this.formGroup = new FormGroup({
        username: new FormControl("", [
          Validators.required,
          Validators.minLength(4),
        ]),
      });
    }

    private validateInput(){
      this.formGroup.controls['username'].markAsTouched();
    }

  private handleError(error: HttpErrorResponse) {

      console.log(error)
    if (error.status === 0) {
      // A client-side or network error occurred. Handle it accordingly.
      this.error = {isError: true, msg: error.message};
    } else {
      // The backend returned an unsuccessful response code.
      console.error(
        `Backend returned code ${error.status}, body was: `, error.error);

      this.error = {isError: true, msg: error.message};

    }
    // Return an observable with a user-facing error message.
    return throwError(() => new Error('Something bad happened; please try again later.'));
  }


    public initLogin(){
      const username = this.username?.value;
      this.validateInput();
      console.log("validate");
      if(!this.formGroup.valid || !username){
        return;
      }
      console.log("start");
     this.authService.startRegisterCeremony(username)
       .subscribe(res => {

         console.log("Subscribe")
       console.log(res);
     }), this.handleError
    }

    public initRegistration(){
     console.log("reg")

      const username = this.username?.value;
return;
      if(!username) return;



    }


  get username() { return this.formGroup.get('username'); }





}

/** Copyright Allianz 2023 */
