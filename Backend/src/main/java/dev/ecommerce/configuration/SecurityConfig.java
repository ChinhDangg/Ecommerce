package dev.ecommerce.configuration;

import dev.ecommerce.auth.jwt.JwtAuthenticationEntryPoint;
import dev.ecommerce.auth.jwt.JwtAuthenticationFilter;
import dev.ecommerce.user.constant.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/media/**").permitAll()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/product/cart").permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/product/**", "POST"),
                                new AntPathRequestMatcher("/api/product/**", "PUT"),
                                new AntPathRequestMatcher("/api/product/**", "DELETE"),
                                new AntPathRequestMatcher("/api/category/**", "POST"),
                                new AntPathRequestMatcher("/api/category/**", "PUT"),
                                new AntPathRequestMatcher("/api/productLine/**", "DELETE"),
                                new AntPathRequestMatcher("/api/productWrapper/**", "POST"),
                                new AntPathRequestMatcher("/api/productWrapper/**", "PUT"),
                                new AntPathRequestMatcher("/api/productWrapper/**", "DELETE"))
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
