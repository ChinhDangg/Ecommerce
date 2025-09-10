package dev.ecommercefrontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${url.gateway}")
    private String gatewayUrl;

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(gatewayUrl+"/.well-known/jwks.json").build();
    }

    @Bean
    BearerTokenResolver bearerTokenResolver() {
        return request -> {
            var h = request.getHeader("Authorization");
            if (h != null && h.startsWith("Bearer ")) return h.substring(7);
            var cookies = request.getCookies();
            if (cookies != null)
                for (var c : cookies) if ("Auth".equals(c.getName())) return c.getValue();
            return null;
        };
    }

    @Bean
    JwtAuthenticationConverter jwtAuthConverter() {
        var gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("scope");
        gac.setAuthorityPrefix("");
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(gac);
        return conv;
    }

    @Bean
    SecurityFilterChain web(HttpSecurity http, JwtDecoder decoder, BearerTokenResolver resolver,
                            JwtAuthenticationConverter conv) throws Exception {
        http.authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/login", "/product/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o ->
                        o.jwt(j -> j.decoder(decoder).jwtAuthenticationConverter(conv))
                        .bearerTokenResolver(resolver)
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint((req,res,e) -> {
                    var r = java.net.URLEncoder.encode(req.getRequestURI(), java.nio.charset.StandardCharsets.UTF_8);
                    res.sendRedirect("/login?r=" + r);
                }));
        return http.build();
    }
}
