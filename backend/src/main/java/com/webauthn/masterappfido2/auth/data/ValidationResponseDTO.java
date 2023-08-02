package com.webauthn.masterappfido2.auth.data;

public record ValidationResponseDTO(boolean isAuthenticated, String username) {

}
