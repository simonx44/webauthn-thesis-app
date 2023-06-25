package com.webauthn.masterappfido2.auth.controller.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteRegistrationDto {


    @NotNull
    @NotBlank
    String credential;
    @NotNull
    @NotBlank
    String username;
    @NotNull
    @NotBlank String credentialName;

}
