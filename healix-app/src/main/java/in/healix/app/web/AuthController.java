package in.healix.app.web;

import in.healix.modules.organization.domain.User;
import in.healix.modules.organization.domain.Role;
import in.healix.modules.organization.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    
    private final String secretString = "healix-secure-high-entropy-jwt-secret-key-32bytes-long";
    private final SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }

        User user = userOpt.get();
        if (!request.getPassword().equals(user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
        }

        List<String> rolesList = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // Token valid for 24h
        String token = Jwts.builder()
                .subject(user.getEmail())
                .claim("tenant_id", user.getTenantId().toString())
                .claim("user_id", user.getId().toString())
                .claim("roles", rolesList)
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();

        return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), user.getTenantId(), rolesList));
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;
        private String email;
        private UUID tenantId;
        private List<String> roles;

        public LoginResponse(String token, String email, UUID tenantId, List<String> roles) {
            this.token = token;
            this.email = email;
            this.tenantId = tenantId;
            this.roles = roles;
        }

        public String getToken() { return token; }
        public String getEmail() { return email; }
        public UUID getTenantId() { return tenantId; }
        public List<String> getRoles() { return roles; }
    }

    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
