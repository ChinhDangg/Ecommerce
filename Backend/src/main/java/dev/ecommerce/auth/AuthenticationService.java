package dev.ecommerce.auth;

import dev.ecommerce.auth.jwt.JwtService;
import dev.ecommerce.user.service.JpaUserDetailService;
import dev.ecommerce.user.constant.Role;
import dev.ecommerce.user.SecurityUser;
import dev.ecommerce.user.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final JpaUserDetailService jpaUserDetailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JpaUserDetailService jpaUserDetailService, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.jpaUserDetailService = jpaUserDetailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        String username = registerRequest.email();
        // username must not yet be registered
        if (jpaUserDetailService.findByUsername(username).isPresent())
            throw new BadCredentialsException("Username already exists");
        else {
            User user = new User(
                    registerRequest.firstName(),
                    registerRequest.lastName(),
                    registerRequest.email(),
                    passwordEncoder.encode(registerRequest.password()),
                    Role.USER
            );
            jpaUserDetailService.save(user);
            return new AuthenticationResponse(
                jwtService.makeAuthenticateCookie(new SecurityUser(user))
            );
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        try {
            String username = authenticationRequest.username();
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, authenticationRequest.password())
            );
            var user = jpaUserDetailService.findByUsername(username).orElseThrow();
            return new AuthenticationResponse(
                jwtService.makeAuthenticateCookie(user)
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
