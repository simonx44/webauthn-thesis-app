package com.webauthn.masterappfido2.auth.controller;

import com.webauthn.masterappfido2.auth.controller.dtos.CompleteAuthDto;
import com.webauthn.masterappfido2.auth.controller.dtos.CompleteCredentialCreationCeremonyDto;
import com.webauthn.masterappfido2.auth.controller.dtos.InitCredentialCreationCeremonyDto;
import com.webauthn.masterappfido2.auth.data.authenticator.Authenticator;
import com.webauthn.masterappfido2.auth.exception.UserRegistrationException;
import com.webauthn.masterappfido2.auth.service.AuthenticationService;
import com.webauthn.masterappfido2.auth.service.CredentialService;
import com.webauthn.masterappfido2.auth.service.RegistrationService;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController()
@RequestMapping("/auth")
public class AuthController {

    private final String SESSION_ISUSERLOGGEDIN = "isUserLoggedIn";
    private final String SESSION_USERNAME = "sessionUserName";

    private RegistrationService registrationService;

    private CredentialService credentialService;

    private AuthenticationService authService;

    AuthController(RegistrationService service, AuthenticationService authService, CredentialService credentialService) {

        this.registrationService = service;
        this.authService = authService;
        this.credentialService = credentialService;
    }


    /**
     * Registration step 1
     *
     * @param data
     * @param session
     * @return
     * @throws Exception
     */
    @PostMapping("/registerInit")
    public ResponseEntity newUserRegistration(
            @Valid @RequestBody InitCredentialCreationCeremonyDto data,
            HttpSession session
    ) throws Exception {
        // Get registration init object
        var registrationOptions = registrationService.initRegistrationCeremony(data);
        // write options in session

        session.setAttribute(data.getName(), registrationOptions);
        return new ResponseEntity(registrationOptions.toCredentialsCreateJson(), HttpStatus.ACCEPTED);
    }


    /**
     * Registration step 2
     *
     * @return
     */
    @PostMapping("/registrationComplete")
    public ResponseEntity completeRegistration(
            @Valid @RequestBody CompleteCredentialCreationCeremonyDto data,
            HttpSession session
    ) throws UserRegistrationException {

        PublicKeyCredentialCreationOptions requestOptions = (PublicKeyCredentialCreationOptions) session.getAttribute(data.getName());

        if (requestOptions == null)
            throw new UserRegistrationException("No active session found. Complete first step");

        this.registrationService.completeRegistration(data, requestOptions);

        var res = new HashMap<String, String>() {{
            this.put("msg", "Registration successfully completed");
        }};

        session.setAttribute(SESSION_ISUSERLOGGEDIN, true);
        session.setAttribute(SESSION_USERNAME, data.getName());
        return new ResponseEntity(res, HttpStatus.ACCEPTED);
    }

    @PostMapping("/login/init")
    public ResponseEntity startLogin(
            @RequestParam String username,
            HttpSession session
    ) throws Exception {

        var request = authService.generateAuthRequest(username);
        session.setAttribute(username, request);
        var response = request.toCredentialsGetJson();
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/login/autofill/init")
    public ResponseEntity startMediationLogin(
            HttpSession session
    ) throws Exception {

        var request = authService.generateMediationAuthRequest();
        session.setAttribute("conditionalRequest", request);
        var response = request.toCredentialsGetJson();
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/login/complete")
    public ResponseEntity completeLogin(
            @Valid @RequestBody CompleteAuthDto data,
            HttpSession session
    ) {


        String sessionAccessKey = data.getUsername().isEmpty() ?  "conditionalRequest" : data.getUsername();

        AssertionRequest request = (AssertionRequest) session.getAttribute(sessionAccessKey);
        var authResponse = this.authService.validateClientAuthResponse(data, request);
        var res = new HashMap<String, String>();
        if (authResponse.isAuthenticated()) {

            session.setAttribute(SESSION_ISUSERLOGGEDIN, true);
            session.setAttribute(SESSION_USERNAME, authResponse.username());
            res.put("msg", "Login successfully completed");

            return new ResponseEntity<>(res, HttpStatus.OK);
        } else {
            res.put("msg", "Login failed");
            session.setAttribute(SESSION_ISUSERLOGGEDIN, false);
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping("/authenticator")
    public ResponseEntity<List<Authenticator>> getUserAuthenticators(HttpSession session) throws Exception {


        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }

        var userId = (String) session.getAttribute(SESSION_USERNAME);

        List<Authenticator> list = credentialService.listAuthenticatorsByUser(userId);

        return new ResponseEntity<>(list, HttpStatus.OK);

    }


    @DeleteMapping("/authenticator/{id}")
    public ResponseEntity deleteUserAuthenticator(@PathVariable Long id, HttpSession session) throws Exception {

        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }

        var sessionUserName = (String) session.getAttribute(SESSION_USERNAME);
        credentialService.deleteAuthenticator(id, sessionUserName);
        return new ResponseEntity<>(null, HttpStatus.OK);

    }


    private boolean isUserLoggedIn(HttpSession session) {
        var sessionLoginState = session.getAttribute(SESSION_ISUSERLOGGEDIN);
        var sessionUserId = session.getAttribute(SESSION_USERNAME);
        boolean isUserLoggedIn = sessionLoginState == null ? false : (boolean) sessionLoginState;
        var userId = sessionUserId == null ? "" : (String) sessionUserId;
        return !userId.isEmpty() && isUserLoggedIn;

    }


    @PostMapping("/passkey")
    public ResponseEntity addNewPublicKeyCredential(
            @Valid @RequestBody InitCredentialCreationCeremonyDto data,
            HttpSession session
    ) throws Exception {

        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }

        var username = (String) session.getAttribute(SESSION_USERNAME);

        // Get registration init object
        var registrationOptions = registrationService.addAdditionalPasskey(data.getName(), username);
        // write options in session

        session.setAttribute(data.getName(), registrationOptions);
        return new ResponseEntity(registrationOptions.toCredentialsCreateJson(), HttpStatus.ACCEPTED);
    }

    @PostMapping("/passkey/complete")
    public ResponseEntity completeAddingNewPasskey(
            @Valid @RequestBody CompleteCredentialCreationCeremonyDto data,
            HttpSession session
    ) throws Exception {

        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }

        PublicKeyCredentialCreationOptions requestOptions = (PublicKeyCredentialCreationOptions) session.getAttribute(data.getName());

        if (requestOptions == null)
            throw new UserRegistrationException("No active session found. Complete first step");

        this.registrationService.completeRegistration(data, requestOptions);

        var res = new HashMap<String, String>() {{
            this.put("msg", "Registration successfully completed");
        }};

        return new ResponseEntity(res, HttpStatus.ACCEPTED);
    }

    @PostMapping("/transaction/init")
    public ResponseEntity initTransactionConfirmation(
            HttpSession session
    ) throws Exception {

        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }

        var username = (String) session.getAttribute(SESSION_USERNAME);

        var request = authService.generateAuthRequest(username);
        session.setAttribute(username, request);
        var response = request.toCredentialsGetJson();
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);

    }

    @PostMapping("/transaction/complete")
    public ResponseEntity completeTransactionConfirmation(
            @Valid @RequestBody CompleteAuthDto data,
            HttpSession session
    ) throws Exception {

        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }
        var username = (String) session.getAttribute(SESSION_USERNAME);
        AssertionRequest request = (AssertionRequest) session.getAttribute(username);
        var authResponse = this.authService.validateClientAuthResponse(data, request);

        var res = new HashMap<String, String>();
        if (authResponse.isAuthenticated()) {
            res.put("msg", "Transaction successfully completed");
            return new ResponseEntity<>(res, HttpStatus.OK);
        } else {
            res.put("msg", "Transaction authorization failed");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/logout")
    public ResponseEntity logoutUser(
            HttpSession session
    ) {

        session.removeAttribute(SESSION_USERNAME);
        session.removeAttribute(SESSION_ISUSERLOGGEDIN);
        session.invalidate();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/getUserInfo")
    public ResponseEntity getUserInfo(
            HttpSession session
    ) throws Exception {

        if (!isUserLoggedIn(session)) {
            throw new Exception("User is not logged in");
        }

        var username = (String) session.getAttribute(SESSION_USERNAME);

        return new ResponseEntity<>(new HashMap<>() {{
            this.put("username", username);
        }}, HttpStatus.OK);
    }


}
