package dev.ecommerce.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        if (!registerRequest.haveAllFields()) {
            throw new IllegalArgumentException("Missing required fields");
        }
        AuthenticationResponse authenticationResponse = authenticationService.register(registerRequest);
        response.setHeader("Location", "http://localhost:8081/home");
        response.addCookie(authenticationResponse.cookie());
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @PostMapping("/authenticate")
    public void authenticate(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        if (!authenticationRequest.haveAllFields()) {
            throw new IllegalArgumentException("Missing required fields");
        }
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);
        response.setHeader("Location", "http://localhost:8081/home");
        response.addCookie(authenticationResponse.cookie());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
