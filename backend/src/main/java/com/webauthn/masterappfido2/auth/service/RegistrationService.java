package com.webauthn.masterappfido2.auth.service;

import com.webauthn.masterappfido2.auth.controller.dtos.CompleteRegistrationDto;
import com.webauthn.masterappfido2.auth.controller.dtos.InitRegistrationCeremonyDto;
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


    public PublicKeyCredentialCreationOptions initRegistrationCeremony(InitRegistrationCeremonyDto userData) throws UserRegistrationException {

        // check if user is already signed up
        var user =  userRepository.findByUsername(userData.getUsername());
        if(user.isPresent()){
            var msg = String.format("The user with the id %s is already registered", userData.getUsername());
            throw new UserRegistrationException(msg);
        }
        // Create user
        UserIdentity userIdentity = UserIdentity.builder()
                    .name(userData.getUsername())
                    .displayName(userData.getDisplayName())
                    .id(generateRandom(32))
                    .build();

        User saveUser = new User(userIdentity);
        userRepository.save(saveUser);
        return createRegistrationOptions(saveUser);

    }



    private PublicKeyCredentialCreationOptions createRegistrationOptions(
            User user
    ) {
        UserIdentity userIdentity = user.transformToUserIdentity();
        StartRegistrationOptions registrationOptions = StartRegistrationOptions.builder()
                .user(userIdentity)
                .build();
        return relyingParty.startRegistration(registrationOptions);
    }

    public void completeRegistration(CompleteRegistrationDto data, PublicKeyCredentialCreationOptions requestOptions) throws UserRegistrationException {

        try {
            Optional<User> user = userRepository.findByUsername(data.getUsername());

            if(user.isEmpty())
                throw new UserRegistrationException("Not found");


           var pkc = PublicKeyCredential.parseRegistrationResponseJson(data.getCredential());

            FinishRegistrationOptions options = FinishRegistrationOptions.builder()
                    .request(requestOptions)
                    .response(pkc)
                    .build();
            RegistrationResult result = relyingParty.finishRegistration(options);
            Authenticator savedAuth = new Authenticator(result, pkc.getResponse(), user.get(), data.getCredentialName());
            authRepository.save(savedAuth);
        } catch (IOException e) {
            throw new UserRegistrationException("Fehler");
        } catch (RegistrationFailedException e) {
            throw new UserRegistrationException("Registration failed");
        }

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
                                .id(credential.getCredentialId())
                                .build())
                .collect(Collectors.toSet());
    }

    /**
     * Über einen usernamen wird die eindeutige UserId beschafft
     *  Wird für im Browser benötigt: navigator.credential.get()
     * @param username
     * @return
     */
    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        var user = userRepository.findByUsername(username);
        return Optional.of(user.get().getHandle());
    }

    /**
     * Anmeldung ohne Nutzernamen
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
        Optional<Authenticator> auth = authRepository.findByCredentialId(credentialId);
        return auth.map(credential ->
                RegisteredCredential.builder()
                        .credentialId(credential.getCredentialId())
                        .userHandle(credential.getUser().getHandle())
                        .publicKeyCose(credential.getPublicKey())
                        .signatureCount(credential.getCount())
                        .build()
        );
    }

    /*
    In a similar way, the lookupAll() function returns a set of RegisteredCredential objects. Instead of validating the authenticator’s signature, this function ensures that there aren’t multiple credentials registered with the same credential ID.
     */
    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        List<Authenticator> auth = authRepository.findAllByCredentialId(credentialId);
        return auth.stream()
                .map(credential ->
                        RegisteredCredential.builder()
                                .credentialId(credential.getCredentialId())
                                .userHandle(credential.getUser().getHandle())
                                .publicKeyCose(credential.getPublicKey())
                                .signatureCount(credential.getCount())
                                .build())
                .collect(Collectors.toSet());
    }

}