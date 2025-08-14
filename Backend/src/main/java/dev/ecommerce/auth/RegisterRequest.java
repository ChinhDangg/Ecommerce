package dev.ecommerce.auth;

public record RegisterRequest(
    String firstname,
    String lastname,
    String username,
    String password
) {

    public boolean haveAllFields() {
        return firstname != null && !firstname.isEmpty() &&
                lastname != null && !lastname.isEmpty() &&
                username != null && !username.isEmpty() &&
                password != null && !password.isEmpty();
    }
}
