import * as base64js from "base64-js";
import {throwError} from "rxjs";

export class AuthObjectMapper {

  public static transformToServerRegistrationCompleteObject(options: any, name: string) {

    const credentials = {
      type: options.type,
      id: options.id,
      response: {
        attestationObject: AuthObjectMapper.uint8arrayToBase64url(options.response.attestationObject),
        clientDataJSON: AuthObjectMapper.uint8arrayToBase64url(options.response.clientDataJSON),
        transports: options.response.getTransports && options.response.getTransports() || [],
      },
      clientExtensionResults: options.getClientExtensionResults(),
    };

    return {credential: JSON.stringify(credentials), name};

  }

  public static mapToCredentialCreationOption(options: any): CredentialCreationOptions {
    const createOptions: CredentialCreationOptions = {
      publicKey: {
        ...options.publicKey,
        challenge: AuthObjectMapper.base64urlToUint8array(options.publicKey.challenge),
        user: {
          ...options.publicKey.user,
          id: AuthObjectMapper.base64urlToUint8array(options.publicKey.user.id),
        },
        excludeCredentials: options.publicKey.excludeCredentials.map((credential: any) => ({
          ...credential,
          id: AuthObjectMapper.base64urlToUint8array(credential.id),
        })),
        extensions: options.publicKey.extensions,
      }
    };
    console.log(createOptions)
    return createOptions;
  }

  public static transformCredentialRequestOptions(options: any): CredentialRequestOptions {
    return {
      publicKey: {
        ...options.publicKey,
        challenge: AuthObjectMapper.base64urlToUint8array(options.publicKey.challenge),
        allowCredentials: options?.publicKey.allowCredentials.map((cred: any) => ({
          type: cred.type,
          id: AuthObjectMapper.base64urlToUint8array(cred.id)
        }))
      }
    };
  }

  public static base64ToJSON(base64: string){

    const transformedToString = atob(base64);
    return JSON.parse(transformedToString);
  }

  public static transformAuthResult(credential: any, username: string) {

    const transformedObj = {
      type: credential?.type,
      id: credential?.id,
      response: {
        authenticatorData: AuthObjectMapper.uint8arrayToBase64url(credential.response.authenticatorData),
        clientDataJSON: AuthObjectMapper.uint8arrayToBase64url(credential.response.clientDataJSON),
        signature: AuthObjectMapper.uint8arrayToBase64url(credential.response.signature),
        userHandle: credential.response.userHandle && AuthObjectMapper.uint8arrayToBase64url(credential.response.userHandle),
      },
      clientExtensionResults: credential.getClientExtensionResults(),
    }
    return {
      credential: JSON.stringify(transformedObj),
      username: username
    };

  }


  public static base64urlToUint8array(base64Bytes: any) {
    const padding = '===='.substring(0, (4 - (base64Bytes.length % 4)) % 4);
    return base64js.toByteArray((base64Bytes + padding).replace(/\//g, "_").replace(/\+/g, "-"));
  }

  public static uint8arrayToBase64url(bytes: any): string {
    if (bytes instanceof Uint8Array) {
      return base64js.fromByteArray(bytes).replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
    } else {
      return this.uint8arrayToBase64url(new Uint8Array(bytes));
    }
  }


}
