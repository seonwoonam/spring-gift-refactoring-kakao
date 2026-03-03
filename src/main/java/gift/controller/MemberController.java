package gift.controller;

import gift.dto.MemberRequest;
import gift.dto.TokenResponse;
import gift.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles member registration and login.
 *
 * @author brian.kim
 * @since 1.0
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final AuthService authService;

    public MemberController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody MemberRequest request) {
        final TokenResponse token = authService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody MemberRequest request) {
        final TokenResponse token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(token);
    }
}
