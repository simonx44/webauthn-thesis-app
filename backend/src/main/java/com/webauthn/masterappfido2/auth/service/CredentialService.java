package com.webauthn.masterappfido2.auth.service;

import com.webauthn.masterappfido2.auth.data.authenticator.Authenticator;
import com.webauthn.masterappfido2.auth.data.authenticator.AuthenticatorRepository;
import com.webauthn.masterappfido2.auth.data.user.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Repository
public class CredentialService implements CredentialRepository {

    @Autowired
    private AuthenticatorRepository authRepository;


    @Autowired
    private UserRepository userRepository;


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
        try {

            var user = userRepository.findByHandle(userHandle.getBytes());
            return Optional.of(user.get().getUsername());

        } catch (Exception e) {
            System.out.println("test");
            throw e;
        }

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

    public List<Authenticator> listAuthenticatorsByUser(String userName) {

        var user = this.userRepository.findByUsername(userName);

        if (user.isEmpty())
            return new ArrayList<>();

        return this.authRepository.findAllByUser(user.get());

    }


    public boolean deleteAuthenticator(Long id, String userName) {

        var user = this.userRepository.findByUsername(userName);

        if (user.isEmpty())
            return false;

        this.authRepository.deleteById(id);
        return true;
    }


}
