package dev.ecommerce.internalService;

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

    @GetMapping(value = "/.well-known/jwks.json", produces = "application/json")
    Map<String,Object> jwks() {
        var jwk = new com.nimbusds.jose.jwk.RSAKey.Builder(k.getPublicKey())
                .keyID(k.getKid())
                .build();
        return Map.of("certs", List.of(jwk.toJSONObject()));
    }
}
