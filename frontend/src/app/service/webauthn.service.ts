import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CompletionApiResult, InitCredentialCreateCeremonyDto} from "../types";
import {concatMap, from, map, Observable} from "rxjs";
import {AuthObjectMapper, AuthTransformationType} from "./AuthObjectMapper";
import {UserService} from "./user.service";


@Injectable({
  providedIn: 'root'
})
export class WebauthnService {

  private static readonly BASE_URL = "http://localhost:8080";

  private static readonly DEFAULT_HEADERS = {withCredentials: true};

  private _abortSignal: AbortSignal | undefined;


  constructor(private client: HttpClient, private userService: UserService) {
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
  public startRegisterCeremony(credentialName: string): Observable<any> {
    const url = `${WebauthnService.BASE_URL}/auth/registerInit`;
    const body: InitCredentialCreateCeremonyDto = {name: credentialName};
    return this.client.post(url, body, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map(res => AuthObjectMapper.mapToCredentialCreationOption(res)),
        concatMap((options) => {

          console.log("GOO");
          console.log(options);
          return this.createAuthenticator(options, credentialName)

        }));
  }

  set abortSignal(value: AbortSignal | undefined) {
    this._abortSignal = value;
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
  public createAuthenticator(options: CredentialCreationOptions, credentialName: string, registrationMode = true) {
   console.log(options)
    const createPK = navigator.credentials.create(options);
    return from(createPK)
      .pipe(
        map((registrationRes) => AuthObjectMapper.transformToServerRegistrationCompleteObject(registrationRes, credentialName)),
        concatMap(body => {
          const url = registrationMode ? `${WebauthnService.BASE_URL}/auth/registrationComplete` : `${WebauthnService.BASE_URL}/auth/passkey/complete`;
          return this.client.post<CompletionApiResult>(url, body, WebauthnService.DEFAULT_HEADERS)
        })
      );
  }


  public startAddCredentialCeremony(credentialName: string): Observable<any> {
    const url = `${WebauthnService.BASE_URL}/auth/passkey`;
    const body: InitCredentialCreateCeremonyDto = {name: credentialName};
    return this.client.post(url, body, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map(res => AuthObjectMapper.mapToCredentialCreationOption(res)),
        concatMap((options) => this.createAuthenticator(options, credentialName, false)));
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
        map((opt) => AuthObjectMapper.transformCredentialRequestOptions(opt, AuthTransformationType.STANDARD)),
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
  public validateAuthenticator(options: CredentialRequestOptions, username: string): Observable<any> {

    console.log("Validate")
console.log(options);
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


  public initAutofillAuthentication(): Observable<any> {
    const url = `${WebauthnService.BASE_URL}/auth/login/autofill/init`;
    return this.client.post(url, null, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map((res) => AuthObjectMapper.transformCredentialRequestOptions(res, AuthTransformationType.CONDITIONAL, this._abortSignal)
        ), concatMap(res => this.validateAuthenticator(res, "")));

  }

  public authenticateUsernameLess(): Observable<any> {
    const url = `${WebauthnService.BASE_URL}/auth/login/autofill/init`;
    return this.client.post(url, null, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map((res) => AuthObjectMapper.transformCredentialRequestOptions(res, AuthTransformationType.USERNAMELESS)
        ),
        concatMap(res => this.validateAuthenticator(res, "")));

  }


  public getUserAuthenticators() {
    const url = `${WebauthnService.BASE_URL}/auth/authenticator`;

    return this.client.get<any>(url, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map(res => res.map((item: any) => ({...item, attestationObject: JSON.parse(item.parsedAttestationObject)}))));

  }

  public deleteAuthenticator(id: number) {
    const url = `${WebauthnService.BASE_URL}/auth/authenticator/${id}`;

    return this.client.delete<any>(url, WebauthnService.DEFAULT_HEADERS);

  }

  public confirmTransaction(moneyToTransfer: number) {
    const url = `${WebauthnService.BASE_URL}/auth/transaction/init`;

    return this.client.post<any>(url, null, WebauthnService.DEFAULT_HEADERS)
      .pipe(
        map((options) => AuthObjectMapper.transformCredentialRequestOptions(options, AuthTransformationType.STANDARD)),
        concatMap(options => {
          const authResult = navigator.credentials.get(options);
          return from(authResult)
            .pipe(
              map(cred => AuthObjectMapper.transformAuthResult(cred, "")),
              concatMap(res => {
                const url = `${WebauthnService.BASE_URL}/auth/transaction/complete`;
                return this.client.post(url, res, WebauthnService.DEFAULT_HEADERS)
              })
            );
        }));

  }


}

