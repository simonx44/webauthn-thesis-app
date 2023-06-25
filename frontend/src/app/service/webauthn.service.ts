import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {InitRegistrationCeremonyDto} from "../types";
import {Observable} from "rxjs";
@Injectable({
  providedIn: 'root'
})
export class WebauthnService {

  private static readonly BASE_URL = "http://localhost:8080";

  constructor(private client: HttpClient) {


}

  public startRegisterCeremony(username:string) : Observable<any> {

    const url = `${WebauthnService.BASE_URL}/registerInit`;

    const body: InitRegistrationCeremonyDto = {username, displayName: username};

    return this.client.post(url, body)

  }




}
