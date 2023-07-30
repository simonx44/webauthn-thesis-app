package com.webauthn.masterappfido2.auth.data.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn.masterappfido2.auth.data.user.User;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.AttestedCredentialData;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Authenticator {

    // wird clientseitig für die Assertion genutzt -> PublicKeyCredentialRequestOptions

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String name;

    @Lob
    @Column(nullable = false)
    private byte[] credentialId;

    @Lob
    @Column(nullable = false)
    private byte[] publicKey;

    @ManyToOne
    private User user;

    // Is this a passkey?
    @Column(nullable = true)
    private Boolean isDiscoverable;

    // Can this credential be backed up (synced)?
    @Column(nullable = true)
    private Boolean isBackupEligible;

    // Is this credential currently backed up?
    @Column(nullable = true)
    private Boolean isBackedUp;

    @Column(nullable = false)
    private Long count;

    @Column(nullable = true)
    private byte[] attestationObject;

    @Column(nullable = true)
    private byte[] clientDataJSON;

    //CUSTOM FOR DEMO PURPOSES
    @Column(nullable = true, columnDefinition = "NVARCHAR(MAX)")
    private String parsedAttestationObject;

    //identifier für einen authenticator
    // Gibt den typ an (model)
    @Lob
    @Column(nullable = true)
    private byte[] aaguid;

    @CreationTimestamp
    private Instant createdOn;

    @UpdateTimestamp
    private Instant lastUpdatedOn;

    public Authenticator(RegistrationResult result,
                         AuthenticatorAttestationResponse response,
                         User user,
                         String name) {

        Optional<AttestedCredentialData> attestationData = response.getAttestation()
                .getAuthenticatorData()
                .getAttestedCredentialData();

        boolean isDiscoverable = result.isDiscoverable().isPresent() ? result.isDiscoverable().get() : null;
        boolean isBackupEligible = result.isBackupEligible();
        boolean isBackUp = result.isBackedUp();


        this.credentialId = result.getKeyId().getId().getBytes();
        this.publicKey = result.getPublicKeyCose().getBytes();
        this.aaguid = attestationData.get().getAaguid().getBytes();
        this.count = result.getSignatureCount();
        this.name = name;
        this.user = user;
        this.parsedAttestationObject = this.extractAttestationObject(response);
        this.clientDataJSON = response.getClientDataJSON().getBytes();
        this.attestationObject = response.getAttestationObject().getBytes();
        this.isDiscoverable = isDiscoverable;
        this.isBackedUp = isBackUp;
        this.isBackupEligible = isBackupEligible;

    }

    private String extractAttestationObject(AuthenticatorAttestationResponse response) {
        try {
            var format = response.getAttestation().getFormat();
            var authData = response.getAttestation().getAuthenticatorData();
            var attStmt = response.getAttestation().getAttestationStatement();


            var authenticator = new HashMap<>();
            authenticator.put("rpIdHash", authData.getRpIdHash());
            authenticator.put("flags", authData.getFlags());
            authenticator.put("counter", authData.getSignatureCounter());
            //attestedCredData
            authenticator.put("attestedCredData", new HashMap() {{
                put("credentialId", authData.getAttestedCredentialData().get().getCredentialId());
                put("credentialPublicKey", authData.getAttestedCredentialData().get().getCredentialPublicKey());
                put("aaguid", authData.getAttestedCredentialData().get().getAaguid());
            }});
            //authenticator.put("extensions", authData.getExtensions());
            var root = new HashMap<String, Object>();
            root.put("authData", authenticator);
            root.put("fmt", format);
            root.put("attStmt", attStmt);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {

            return "";
        }


    }


}