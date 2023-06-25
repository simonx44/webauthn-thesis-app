package com.webauthn.masterappfido2.auth.service;

import com.webauthn.masterappfido2.auth.controller.dtos.CompleteAuthDto;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.springframework.stereotype.Repository;

import java.io.IOException;


@Repository
public class AuthService {

    private RelyingParty relyingParty;



    public AssertionRequest generateAuthRequest(String username){

        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(username)
                .build());

        return request;

    }

    public boolean validateClientAuthResponse(CompleteAuthDto data, AssertionRequest assertionRequest){

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc;
            pkc = PublicKeyCredential.parseAssertionResponseJson(data.getCredential());
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertionRequest)
                    .response(pkc)
                    .build());
           return result.isSuccess();
        } catch (IOException e) {
            throw new RuntimeException("Authentication failed", e);
        } catch (AssertionFailedException e) {
            throw new RuntimeException("Authentication failed", e);
        }

    }




}
