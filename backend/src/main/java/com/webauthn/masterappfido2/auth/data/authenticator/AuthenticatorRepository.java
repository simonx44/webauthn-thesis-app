package com.webauthn.masterappfido2.auth.data.authenticator;

import com.webauthn.masterappfido2.auth.data.authenticator.Authenticator;
import com.webauthn.masterappfido2.auth.data.user.User;
import com.yubico.webauthn.data.ByteArray;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AuthenticatorRepository extends CrudRepository<Authenticator, Long> {

    Optional<Authenticator> findByCredentialId(byte[] credentialId);
    List<Authenticator> findAllByUser (User user);
    List<Authenticator> findAllByCredentialId(byte[] credentialId);

}
