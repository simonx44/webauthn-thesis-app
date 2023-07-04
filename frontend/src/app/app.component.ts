import {Component, TemplateRef, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {NxDialogService, NxModalRef} from '@aposin/ng-aquila/modal';
import {WebauthnService} from "./service/webauthn.service";
import {HttpErrorResponse} from "@angular/common/http";
import {catchError, flatMap, throwError} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent {


}

