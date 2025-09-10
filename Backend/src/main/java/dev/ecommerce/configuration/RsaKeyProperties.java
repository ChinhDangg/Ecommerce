package dev.ecommerce.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@ConfigurationProperties(prefix = "rsa")
@Getter
@Setter
public class RsaKeyProperties {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @Setter(lombok.AccessLevel.NONE) // ensure binder cannot try to set it
    private String kid;

    @PostConstruct
    void computeKid() {
        this.kid = computeKid(publicKey);
    }

    private String computeKid(RSAPublicKey publicKey) {
        try {
            byte[] der = publicKey.getEncoded(); // X.509 SubjectPublicKeyInfo
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(der);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute kid", e);
        }
    }
}
