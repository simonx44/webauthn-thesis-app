import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import {AuthComponent } from "./views/auth/auth.component"
import {HomeComponent} from "./views/home/home.component";
import {TransactionStubComponent} from "./views/transaction-stub/transaction-stub.component";


const routes: Routes = [
  { path: 'auth', component: AuthComponent },
  { path: 'home', component: HomeComponent },
  { path: 'transaction', component: TransactionStubComponent },
  { path: '',   redirectTo: '/auth', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
