package dev.ecommerce.auth;

public record AuthenticationRequest(
    String username,
    String password
) {
    public boolean haveAllFields() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }
}
