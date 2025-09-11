package dev.ecommerce.internalService;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import dev.ecommerce.configuration.RsaKeyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class InternalController {

    private final RsaKeyProperties k;

    @GetMapping(value = "/api/.well-known/jwks.json", produces = "application/json")
    Map<String,Object> jwks() {
        var jwk = new com.nimbusds.jose.jwk.RSAKey.Builder(k.getPublicKey())
                .keyID(k.getKid())
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();
        return Map.of("keys", List.of(jwk.toJSONObject()));
    }
}
