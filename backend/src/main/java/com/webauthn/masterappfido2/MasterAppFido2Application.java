package com.webauthn.masterappfido2;

import com.webauthn.masterappfido2.auth.config.WebAuthNProperties;
import com.webauthn.masterappfido2.auth.service.RegistrationService;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MasterAppFido2Application {

	public static void main(String[] args) {
		SpringApplication.run(MasterAppFido2Application.class, args);
	}

	@Bean
	public RelyingParty relyingParty(RegistrationService registrationService,
									 WebAuthNProperties properties) {
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
				.id(properties.getHostName())
				.name(properties.getDisplay())
				.build();

		return RelyingParty.builder()
				.identity(rpIdentity)
				.credentialRepository(registrationService)
				.origins(properties.getOrigin())
				.build();
	}

}
