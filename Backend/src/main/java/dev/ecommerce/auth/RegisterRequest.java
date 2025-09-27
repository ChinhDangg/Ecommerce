package dev.ecommerce.auth;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.regex.Pattern;

public record RegisterRequest(
    @NotBlank @Length(max = 20)
    String firstName,
    @NotBlank @Length(max = 20)
    String lastName,
    @NotBlank @Length(max = 20)
    String email,
    @NotBlank @Length(min = 8, max = 30)
    String password,
    @NotBlank @Length(min = 8, max = 30)
    String confirmPassword
) {

    public boolean checkAllFields() {
        return checkPassword();
    }

    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public boolean checkPassword() {
        if (!password.equals(confirmPassword))
            return false;
        return pattern.matcher(password).matches();
    }
}
