package dev.ecommercefrontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${url.gateway}")
    private String gatewayUrl;

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withJwkSetUri(gatewayUrl+"/api/.well-known/jwks.json")
                .build();
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
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(""); // Removes the 'ROLE_' prefix
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

    // to avoid token check for public (like token cookies present or in header)
    @Bean @Order(1)
    SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/", "/login", "/register", "/product/**", "/css/**", "/js/**", "/images/**")
                .authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    @Bean @Order(2)
    SecurityFilterChain protectedChain(HttpSecurity http, JwtDecoder dec,
                                       BearerTokenResolver resolver, JwtAuthenticationConverter conv) throws Exception {
        http.authorizeHttpRequests(a -> a
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER","ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2
                                .authenticationEntryPoint((req,res,e) -> {
                                    String uri = req.getRequestURI();
                                    String query = req.getQueryString();
                                    String fullPath = (query != null ? uri + "?" + query : uri);

                                    String r = URLEncoder.encode(fullPath, StandardCharsets.UTF_8);
                                    res.sendRedirect(gatewayUrl + "/login?r=" + r);
                                })
                                .jwt(j -> j.decoder(dec).jwtAuthenticationConverter(conv))
                                .bearerTokenResolver(resolver))
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint((req,res,e) -> {
                            String uri = req.getRequestURI();
                            String query = req.getQueryString();
                            String fullPath = (query != null ? uri + "?" + query : uri);

                            String r = URLEncoder.encode(fullPath, StandardCharsets.UTF_8);
                            res.sendRedirect(gatewayUrl + "/login?r=" + r);
                        }));
        return http.build();
    }
}
