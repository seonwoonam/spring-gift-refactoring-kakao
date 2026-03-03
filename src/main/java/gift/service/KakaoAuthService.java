package gift.service;

import gift.auth.JwtProvider;
import gift.client.KakaoLoginClient;
import gift.config.KakaoLoginProperties;
import gift.dto.TokenResponse;
import gift.model.Member;
import gift.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/*
 * Handles the Kakao OAuth2 login flow.
 * 1. buildLoginUrl() generates the Kakao authorization URL
 * 2. processCallback() receives the authorization code, exchanges it for an access token,
 *    retrieves user info, auto-registers the member if new, and issues a service JWT
 */
@Service
public class KakaoAuthService {
    private final KakaoLoginProperties properties;
    private final KakaoLoginClient kakaoLoginClient;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public KakaoAuthService(
        KakaoLoginProperties properties,
        KakaoLoginClient kakaoLoginClient,
        MemberRepository memberRepository,
        JwtProvider jwtProvider
    ) {
        this.properties = properties;
        this.kakaoLoginClient = kakaoLoginClient;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public String buildLoginUrl() {
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId())
            .queryParam("redirect_uri", properties.redirectUri())
            .queryParam("scope", "account_email,talk_message")
            .build()
            .toUriString();
    }

    public TokenResponse processCallback(String code) {
        KakaoLoginClient.KakaoTokenResponse kakaoToken = kakaoLoginClient.requestAccessToken(code);
        KakaoLoginClient.KakaoUserResponse kakaoUser = kakaoLoginClient.requestUserInfo(kakaoToken.accessToken());
        String email = kakaoUser.email();

        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> new Member(email));
        member.updateKakaoAccessToken(kakaoToken.accessToken());
        memberRepository.save(member);

        String token = jwtProvider.createToken(member.getEmail());
        return new TokenResponse(token);
    }
}
