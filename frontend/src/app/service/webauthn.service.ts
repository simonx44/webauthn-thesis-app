import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {CompleteRegistrationDto, CompletionApiResult, InitRegistrationCeremonyDto} from "../types";
import {catchError, concatMap, flatMap, from, map, Observable, of, throwError} from "rxjs";
import * as base64js from 'base64-js';
import {AuthObjectMapper} from "./AuthObjectMapper";

@Injectable({
  providedIn: 'root'
})
export class WebauthnService {

  private static readonly BASE_URL = "http://localhost:8080";

  private static readonly DEFAULT_HEADERS = {withCredentials: true};

  constructor(private client: HttpClient) {
  }

  /**
   * Registration step 1:
   *
   * Init registration ceremony
   * 1.Step: Call server rest endpoint to get random challenge
   * 2.Step: Conversion of the response into a webauthn compatible object
   * 3.Step: Call browser Web Authentication api (function createAuthenticator)
   * @param username
   */
  public startRegisterCeremony(username: string): Observable<any> {
    const url = `${WebauthnService.BASE_URL}/auth/registerInit`;
    const body: InitRegistrationCeremonyDto = {username, displayName: username};
    return this.client.post(url, body, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map(res => AuthObjectMapper.mapToCredentialCreationOption(res)),
        concatMap((options) => this.createAuthenticator(options, username)));
  }


  /**
   * Registration step 2:
   *
   * Create an authenticator and the related private and public keys
   * 1. Call Web Authentication Api - create
   * 2. Conversion of the response int server compatible object
   * 3. Register authenticator on the server by sending creation response
   * @param options
   * @param username
   */
  public createAuthenticator(options: CredentialCreationOptions, username: string) {
    const createPK = navigator.credentials.create(options);
    return from(createPK)
      .pipe(
        map((registrationRes) => AuthObjectMapper.transformToServerRegistrationCompleteObject(registrationRes, username)),
        concatMap(body => {
          const url = `${WebauthnService.BASE_URL}/auth/registrationComplete`;
          return this.client.post<CompletionApiResult>(url, body, WebauthnService.DEFAULT_HEADERS)
        })
      );
  }


  /**
   * Authenticate step 1: Get challenge from server
   *
   * Authenticate a user based on a registered device
   * Step 1: Call backend api to get random challenge
   * Step 2: Conversion of the response int WebauthN compatible object
   * Step 2: Call browser Web Authentication api (function get)
   * @param username
   */
  public authenticateUser(username: string): Observable<any> {
    const url = `${WebauthnService.BASE_URL}/auth/login/init?username=${username}`;
    return this.client.post(url, null, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map(AuthObjectMapper.transformCredentialRequestOptions),
        concatMap(res => this.validateAuthenticator(res, username)));
  }

  /**
   * Authenticate step 1: Get challenge from server
   *
   * Step 1: Call backend api to get random challenge
   * Step 2: Conversion of the response int WebauthN compatible object
   * Step 2: Call browser Web Authentication api (function get)
   * @param authOptions
   * @param username
   * @private
   */
  private validateAuthenticator(options: CredentialRequestOptions, username: string): Observable<any> {
    const authResult = navigator.credentials.get(options);
    return from(authResult)
      .pipe(
        map(cred => AuthObjectMapper.transformAuthResult(cred, username)),
        concatMap(res => {
          const url = `${WebauthnService.BASE_URL}/auth/login/complete`;
          return this.client.post(url, res, WebauthnService.DEFAULT_HEADERS)
        })
      );

  }


}

