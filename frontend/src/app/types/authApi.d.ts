export type InitCredentialCreateCeremonyDto = {
  name: string;
}

export type CompleteRegistrationDto = {
  credential: string;
  username: string;
  credentialName: string;
}

export type CompletionApiResult = {
  msg: string
}


export type PasskeyT = {

  aaguid: string,
  attestationObject: {
    attStmt: Record<string, any>,
    authData: {
      "rpIdHash": string,
      "flags": {
        "UP": boolean,
        "UV": boolean
        "BE": boolean
        "BS": boolean
        "AT": boolean
        "ED": boolean
      },
      "attestedCredData": {
        "aaguid": string,
        "credentialId": string,
        "credentialPublicKey": string
      },
      "counter": 4
    }
    fmt: string
  }
  id: number,
  name: string,
  publicKey: string,
  count: number,
  lastUpdatedOn: string,
  createdOn: string,
  isBackedUp: boolean,
  isBackupEligible: boolean,
  isDiscoverable: boolean,
  user: {
    id: number,
    username: string,
    displayName: string,
    handle: string
  },


}

