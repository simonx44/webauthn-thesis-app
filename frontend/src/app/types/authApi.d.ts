export type InitRegistrationCeremonyDto = {
  username: string;
  displayName: string;
}

export type CompleteRegistrationDto = {
  credential: string;
  username: string;
  credentialName: string;
}

export type CompletionApiResult = {
  msg: string
}



