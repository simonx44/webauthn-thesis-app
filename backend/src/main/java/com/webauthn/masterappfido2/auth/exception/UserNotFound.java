package com.webauthn.masterappfido2.auth.exception;

public class UserNotFound extends Exception {
    public UserNotFound(String message) {
        super(message);
    }
}
