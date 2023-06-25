package com.webauthn.masterappfido2.auth.data.user;


import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name="WebAuthUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // wird durch Nutzer bestimmt -> eignet sich aber nicht zur Identifizierung
    @Column(nullable = false, unique = true)
    private String username;

    // wird durch Nutzer bestimmt -> eignet sich aber nicht zur Identifizierung
    @Column(nullable = false, unique = true)
    private String displayedName;

    // identifikation erfolgt im Anschluss über byte sequence byte 64
    @Lob
    @Column(nullable = false, length = 64)
    private ByteArray handle;

    public User(UserIdentity user) {
        this.handle = user.getId();
        this.username = user.getName();
        this.displayedName = user.getDisplayName();
    }

    public UserIdentity transformToUserIdentity() {
        return UserIdentity.builder()
                .name(getUsername())
                .displayName(getDisplayedName())
                .id(getHandle())
                .build();
    }


}
