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
public class RegistrationService implements CredentialRepository {


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
                .authenticatorAttachment(AuthenticatorAttachment.CROSS_PLATFORM)
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


    /**
     * Vor der Registierung eines neuen Authenticators genutzt
     * Sämtliche regististrierten Geräte werden an Nutzer gegben
     *
     * @param username
     * @return
     */
    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        var user = userRepository.findByUsername(username);

        List<Authenticator> auth = authRepository.findAllByUser(user.get());
        return auth.stream()
                .map(credential ->
                        PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(credential.getCredentialId()))
                                .build())
                .collect(Collectors.toSet());
    }

    /**
     * Über einen usernamen wird die eindeutige UserId beschafft
     * Wird für im Browser benötigt: navigator.credential.get()
     *
     * @param username
     * @return
     */
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        var user = userRepository.findByUsername(username);
        return Optional.of(new ByteArray(user.get().getHandle()));
    }

    /**
     * Anmeldung ohne Nutzernamen
     *
     * @param userHandle
     * @return
     */
    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        var user = userRepository.findByHandle(userHandle);
        return Optional.of(user.get().getUsername());
    }

    /**
     * Übernimmt die verifizierung
     *
     * @param credentialId
     * @param userHandle
     * @return
     */
    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {

        Optional<Authenticator> auth = authRepository.findByCredentialId(credentialId.getBytes());
        return auth.map(credential ->
                RegisteredCredential.builder()
                        .credentialId(new ByteArray(credential.getCredentialId()))
                        .userHandle(new ByteArray(credential.getUser().getHandle()))
                        .publicKeyCose(new ByteArray(credential.getPublicKey()))
                        .signatureCount(credential.getCount())
                        .build()
        );
    }

    /*
    In a similar way, the lookupAll() function returns a set of RegisteredCredential objects. Instead of validating the authenticator’s signature, this function ensures that there aren’t multiple credentials registered with the same credential ID.
     */
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        var credentialIdBytes = credentialId.getBytes();
        List<Authenticator> auth = authRepository.findAllByCredentialId(credentialIdBytes);
        return auth.stream()
                .map(credential ->
                        RegisteredCredential.builder()
                                .credentialId(new ByteArray(credential.getCredentialId()))
                                .userHandle(new ByteArray(credential.getUser().getHandle()))
                                .publicKeyCose(new ByteArray(credential.getPublicKey()))
                                .signatureCount(credential.getCount())
                                .build())
                .collect(Collectors.toSet());
    }

}