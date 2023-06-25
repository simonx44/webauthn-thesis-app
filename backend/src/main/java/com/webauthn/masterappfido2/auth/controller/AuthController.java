package com.webauthn.masterappfido2.auth.controller;

import com.webauthn.masterappfido2.auth.controller.dtos.CompleteAuthDto;
import com.webauthn.masterappfido2.auth.controller.dtos.CompleteRegistrationDto;
import com.webauthn.masterappfido2.auth.controller.dtos.InitRegistrationCeremonyDto;
import com.webauthn.masterappfido2.auth.exception.UserRegistrationException;
import com.webauthn.masterappfido2.auth.service.AuthService;
import com.webauthn.masterappfido2.auth.service.RegistrationService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/auth")
public class AuthController {



    private RegistrationService registrationService;

    private AuthService authService;

    AuthController(RegistrationService service, AuthService authService) {

        this.registrationService = service;
        this.authService = authService;
    }


    /**
     * Registration step 1
     * @param data
     * @param session
     * @return
     * @throws Exception
     */
    @PostMapping("/registerInit")
    public ResponseEntity newUserRegistration(
            @Valid  @RequestBody InitRegistrationCeremonyDto data,
            HttpSession session
    ) throws Exception {
        // Get registration init object
        var registrationOptions = registrationService.initRegistrationCeremony(data);
        // write options in session
        session.setAttribute(data.getDisplayName(), registrationOptions);
        return new ResponseEntity(registrationOptions.toCredentialsCreateJson(), HttpStatus.ACCEPTED);
    }


    /**
     * Registration step 2
     * @return
     */
    @PostMapping("/registrationComplete")
    @ResponseBody
    public ResponseEntity completeRegistration(
            @Valid @RequestBody CompleteRegistrationDto data,
            HttpSession session
    ) throws UserRegistrationException {

        PublicKeyCredentialCreationOptions requestOptions = (PublicKeyCredentialCreationOptions) session.getAttribute(data.getUsername());

        if(requestOptions == null)
            throw new UserRegistrationException("No active session found. Complete first step");

        this.registrationService.completeRegistration(data, requestOptions);

        return new ResponseEntity("Registration successfully completed", HttpStatus.ACCEPTED);
    }


    @PostMapping("/login/init")
    @ResponseBody
    public ResponseEntity startLogin(
            @RequestParam String username,
            HttpSession session
    ) throws Exception {
        var request = authService.generateAuthRequest(username);
        session.setAttribute(username, request);
        var response = request.toCredentialsGetJson();
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

    }

    @PostMapping("/login/complete")
    public ResponseEntity completeLogin(
            @Valid @RequestBody CompleteAuthDto data,
            HttpSession session
    ) {
        AssertionRequest request = (AssertionRequest) session.getAttribute(data.getUsername());
        boolean isLoginSuccessful = this.authService.validateClientAuthResponse(data, request);
        if(isLoginSuccessful){
            return new ResponseEntity<>(null, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

}
