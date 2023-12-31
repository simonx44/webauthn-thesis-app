package com.webauthn.masterappfido2.auth.service;

import com.webauthn.masterappfido2.auth.controller.dtos.CompleteAuthDto;
import com.webauthn.masterappfido2.auth.data.ValidationResponseDTO;
import com.webauthn.masterappfido2.auth.data.authenticator.Authenticator;
import com.webauthn.masterappfido2.auth.data.authenticator.AuthenticatorRepository;
import com.webauthn.masterappfido2.auth.data.user.UserRepository;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.UserVerificationRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class AuthenticationService {

    @Autowired
    private RelyingParty relyingParty;

    @Autowired
    private AuthenticatorRepository authenticatorRepository;

    @Autowired
    private CredentialService credentialService;

    public AssertionRequest generateAuthRequest(String username) {

        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(username)
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build());

        return request;

    }

    public AssertionRequest generateMediationAuthRequest() {

        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build());

        return request;

    }


    public ValidationResponseDTO validateClientAuthResponse(CompleteAuthDto data, AssertionRequest assertionRequest) {

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc;
            pkc = PublicKeyCredential.parseAssertionResponseJson(data.getCredential());
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertionRequest)
                    .response(pkc)
                    .build());


            if (!result.isSuccess()) {
                throw new Exception();
            }
            updateCredential(result);

            var username = this.credentialService.getUsernameForUserHandle(pkc.getResponse().getUserHandle().orElseThrow());

            return new ValidationResponseDTO(true, username.orElseThrow());

        } catch (Exception e) {
            throw new RuntimeException("Authentication failed", e);
        }

    }

    /**
     * Updates the credential/authenticator entry after authentication
     *
     * @param result - AssertionResult
     * @throws Exception
     */
    private void updateCredential(AssertionResult result) throws Exception {

        var credential = authenticatorRepository.findByCredentialId(result.getCredentialId().getBytes());
        if (credential.isEmpty())
            throw new Exception("Credential not found");

        //update counter !!!!
        Authenticator userCredential = credential.get();
        userCredential.setCount(result.getSignatureCount());
        userCredential.setIsBackedUp(result.isBackedUp());

        this.authenticatorRepository.save(userCredential);

    }


}
