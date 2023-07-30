package com.webauthn.masterappfido2.auth.service;

import com.webauthn.masterappfido2.auth.data.authenticator.Authenticator;
import com.webauthn.masterappfido2.auth.data.authenticator.AuthenticatorRepository;
import com.webauthn.masterappfido2.auth.data.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticatorService {


    private AuthenticatorRepository authenticatorRepository;
    private UserRepository userRepository;


    public AuthenticatorService(AuthenticatorRepository authenticatorRepository, UserRepository userRepository) {
        this.authenticatorRepository = authenticatorRepository;
        this.userRepository = userRepository;
    }


    public List<Authenticator> listAuthenticatorsByUser(String userName) {

        var user = this.userRepository.findByUsername(userName);

        if (user.isEmpty())
            return new ArrayList<>();

        return this.authenticatorRepository.findAllByUser(user.get());

    }


    public boolean deleteAuthenticator(Long id, String userName) {

        var user = this.userRepository.findByUsername(userName);

        if (user.isEmpty())
            return false;

        this.authenticatorRepository.deleteById(id);
        return true;
    }

}
