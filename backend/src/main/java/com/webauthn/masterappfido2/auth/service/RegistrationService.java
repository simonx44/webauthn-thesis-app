package com.webauthn.masterappfido2.auth.service;

import com.webauthn.masterappfido2.auth.controller.dtos.CompleteCredentialCreationCeremonyDto;
import com.webauthn.masterappfido2.auth.controller.dtos.InitCredentialCreationCeremonyDto;
import com.webauthn.masterappfido2.auth.data.authenticator.Authenticator;
import com.webauthn.masterappfido2.auth.data.authenticator.AuthenticatorRepository;
import com.webauthn.masterappfido2.auth.data.user.User;
import com.webauthn.masterappfido2.auth.data.user.UserRepository;
import com.webauthn.masterappfido2.auth.exception.UserRegistrationException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Repository
public class RegistrationService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticatorRepository authRepository;

    @Autowired
    private RelyingParty relyingParty;


    public PublicKeyCredentialCreationOptions initRegistrationCeremony(InitCredentialCreationCeremonyDto userData) throws UserRegistrationException {

        // check if user is already signed up
        User user = null;
        var userOptional = userRepository.findByUsername(userData.getName());

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // If user is present, check if user has already registered an authenticator
            List<Authenticator> authenticators = authRepository.findAllByUser(user);
            if (authenticators.size() > 0) {
                var msg = String.format("The user with the id %s is already registered", userData.getName());
                throw new UserRegistrationException(msg);
            }
        } else {
            // Create user
            UserIdentity userIdentity = UserIdentity.builder()
                    .name(userData.getName())
                    .displayName(userData.getName())
                    .id(generateRandom(32))
                    .build();
            User saveUser = new User(userIdentity);
            userRepository.save(saveUser);
            user = saveUser;
            user.setDisplayedName("Passkey " + userData.getName());
        }
        return createRegistrationOptions(user);

    }


    private PublicKeyCredentialCreationOptions createRegistrationOptions(
            User user
    ) {

        var authSelectionCriteria = AuthenticatorSelectionCriteria.builder()
                .residentKey(ResidentKeyRequirement.REQUIRED)
                .userVerification(UserVerificationRequirement.REQUIRED)
                .build();

        UserIdentity userIdentity = user.transformToUserIdentity();
        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
                .user(userIdentity)
                .timeout(30000)
                .authenticatorSelection(authSelectionCriteria)
                .build();
        return relyingParty.startRegistration(registrationOptions);
    }

    public void completeRegistration(CompleteCredentialCreationCeremonyDto data, PublicKeyCredentialCreationOptions requestOptions) throws UserRegistrationException {

        try {
            Optional<User> user = userRepository.findByUsername(requestOptions.getUser().getName());

            if (user.isEmpty())
                throw new UserRegistrationException("Not found");


            var pkc = PublicKeyCredential.parseRegistrationResponseJson(data.getCredential());

            FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                    .request(requestOptions)
                    .response(pkc)
                    .build();
            RegistrationResult result = relyingParty.finishRegistration(options);

            var displayedName = requestOptions.getUser().getDisplayName();

            Authenticator savedAuth = new Authenticator(result, pkc.getResponse(), user.get(), displayedName);
            authRepository.save(savedAuth);
        } catch (IOException e) {
            throw new UserRegistrationException("Fehler");
        } catch (RegistrationFailedException e) {
            throw new UserRegistrationException("Registration failed");
        }

    }


    public PublicKeyCredentialCreationOptions addAdditionalPasskey(String credentialName, String username) throws Exception {

        User user = null;
        var userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty())
            throw new Exception("User does not exist");

        user = userOptional.get();

        // If user is present, check if user has already registered an authenticator
        List<Authenticator> authenticators = authRepository.findAllByUser(user);

        if (authenticators.stream().anyMatch(authenticator -> authenticator.getName() == credentialName))
            throw new Error("Passkey does already exist");

        user.setDisplayedName(credentialName);

        return createRegistrationOptions(user);


    }


    private ByteArray generateRandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }


}