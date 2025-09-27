package dev.ecommerce.auth;

import dev.ecommerce.auth.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Value("${url.gateway}")
    private String gatewayURL;
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        if (!registerRequest.checkAllFields()) {
            throw new BadCredentialsException("Fields requirement not met.");
        }
        AuthenticationResponse authenticationResponse = authenticationService.register(registerRequest);
        response.setHeader("Location", gatewayURL);
        response.addHeader(HttpHeaders.SET_COOKIE, authenticationResponse.cookie().toString());
        //response.addCookie(authenticationResponse.cookie());
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @PostMapping("/authenticate")
    public void authenticate(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        if (!authenticationRequest.haveAllFields()) {
            throw new IllegalArgumentException("Missing required fields");
        }
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);
        response.setHeader("Location", gatewayURL);
        System.out.println("Cookie:");
        System.out.println(authenticationResponse.cookie().getName());
        System.out.println(authenticationResponse.cookie().getValue());
        response.addHeader(HttpHeaders.SET_COOKIE, authenticationResponse.cookie().toString());
        //response.addCookie(authenticationResponse.cookie());
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @PostMapping("/sign-out")
    public void signOut(HttpServletResponse response) {
        response.addCookie(JwtService.removeAuthCookie());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}
