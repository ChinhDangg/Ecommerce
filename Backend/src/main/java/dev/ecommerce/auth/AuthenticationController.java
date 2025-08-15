package dev.ecommerce.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Value("${url.front-end}")
    private String feURL;
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
        response.setHeader("Location", feURL);
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
        response.setHeader("Location", feURL);
        System.out.println("Cookie:");
        System.out.println(authenticationResponse.cookie().getName());
        System.out.println(authenticationResponse.cookie().getValue());
        response.addHeader(HttpHeaders.SET_COOKIE, authenticationResponse.cookie().toString());
        //response.addCookie(authenticationResponse.cookie());
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
