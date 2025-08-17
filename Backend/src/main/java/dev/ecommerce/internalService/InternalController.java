package dev.ecommerce.internalService;

import dev.ecommerce.user.constant.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
public class InternalController {

    @GetMapping("/check-admin")
    public boolean checkAdmin(HttpServletRequest request, Authentication authentication) {
        String frontendKey = request.getHeader("X-Frontend-Key");

        // Verify the frontend key
        if (!"my-very-secret-key".equals(frontendKey)) {
            throw new AccessDeniedException("Not from authorized frontend");
        }

        // Verify the user is authenticated and has ADMIN role
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.name()));
    }
}
