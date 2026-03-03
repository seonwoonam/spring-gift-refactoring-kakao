package gift.controller;

import gift.dto.TokenResponse;
import gift.service.KakaoAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth/kakao")
public class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;

    public KakaoAuthController(KakaoAuthService kakaoAuthService) {
        this.kakaoAuthService = kakaoAuthService;
    }

    @GetMapping(path = "/login")
    public ResponseEntity<Void> login() {
        String kakaoAuthUrl = kakaoAuthService.buildLoginUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, kakaoAuthUrl)
            .build();
    }

    @GetMapping(path = "/callback")
    public ResponseEntity<TokenResponse> callback(@RequestParam("code") String code) {
        TokenResponse response = kakaoAuthService.processCallback(code);
        return ResponseEntity.ok(response);
    }
}
