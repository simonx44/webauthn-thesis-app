import {HttpClientJsonpModule, HttpClientModule} from '@angular/common/http';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RouterModule} from '@angular/router';
import {NxButtonModule} from '@aposin/ng-aquila/button';
import {NxCheckboxModule} from '@aposin/ng-aquila/checkbox';
import {NxDocumentationIconModule} from '@aposin/ng-aquila/documentation-icons';
import {NxDropdownModule} from '@aposin/ng-aquila/dropdown';
import {NxFooterModule} from '@aposin/ng-aquila/footer';
import {NxFormfieldModule} from '@aposin/ng-aquila/formfield';
import {NxGridModule} from '@aposin/ng-aquila/grid';
import {NxHeadlineModule} from '@aposin/ng-aquila/headline';
import {NxIconModule} from '@aposin/ng-aquila/icon';
import {NxInputModule} from '@aposin/ng-aquila/input';
import {NxLinkModule} from '@aposin/ng-aquila/link';
import {NX_MESSAGE_TOAST_DEFAULT_CONFIG, NxMessageModule} from '@aposin/ng-aquila/message';
import {NxModalModule} from '@aposin/ng-aquila/modal';
import {NxOverlayModule} from '@aposin/ng-aquila/overlay';
import {NxPopoverModule} from '@aposin/ng-aquila/popover';
import {NxSmallStageModule} from '@aposin/ng-aquila/small-stage';
import {NxAccordionModule} from '@aposin/ng-aquila/accordion';


import {AppComponent} from './app.component';
import {NxHeaderModule} from "@aposin/ng-aquila/header";
import {HeaderComponent} from './components/header/header.component';
import {AuthComponent} from './views/auth/auth.component';
import {AppRoutingModule} from "./app-routing.module";
import {NxSpinnerModule} from "@aposin/ng-aquila/spinner";
import {HomeComponent} from './views/home/home.component';
import {NxCardModule} from '@aposin/ng-aquila/card';
import {
  AuthCreateResultParserComponent
} from './components/auth-create-result-parser/auth-create-result-parser.component';
import {PasskeyComponent} from './components/passkey/passkey.component';
import {NxTabsModule} from '@aposin/ng-aquila/tabs';
import { TransactionStubComponent } from './views/transaction-stub/transaction-stub.component';
import { NxNumberStepperModule } from '@aposin/ng-aquila/number-stepper';


@NgModule({
  declarations: [AppComponent,  HeaderComponent, AuthComponent, HomeComponent, AuthCreateResultParserComponent, PasskeyComponent, TransactionStubComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    HttpClientJsonpModule,
    HttpClientModule,
    ReactiveFormsModule,
    RouterModule.forRoot([]),
    NxButtonModule,
    NxCheckboxModule,
    NxDocumentationIconModule,
    NxDropdownModule,
    NxNumberStepperModule,
    NxFooterModule,
    NxFormfieldModule,
    NxGridModule,
    NxHeadlineModule,
    NxIconModule,
    NxInputModule,
    NxLinkModule,
    NxMessageModule,
    NxModalModule,
    NxOverlayModule,
    NxPopoverModule,
    NxSmallStageModule,
    NxHeaderModule,
    NxSpinnerModule,
    NxCardModule,
    NxTabsModule,
    NxAccordionModule,

  ],
  bootstrap: [AppComponent],
})
export class AppModule {
}

/** Copyright Allianz 2023 */
