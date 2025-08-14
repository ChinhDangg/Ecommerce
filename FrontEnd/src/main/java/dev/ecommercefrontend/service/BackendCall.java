package dev.ecommercefrontend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

@Service
public class BackendCall {

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean checkHasAdminRole(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        // Copy all headers from the original request
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.add(headerName, request.getHeader(headerName));
        }

        headers.add("X-Frontend-Key", "my-very-secret-key");

        // Forward to backend
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(
                "http://localhost:8080/api/internal/check-admin",
                HttpMethod.GET,
                entity,
                Boolean.class
        );

        return Boolean.TRUE.equals(response.getBody());
    }
}
