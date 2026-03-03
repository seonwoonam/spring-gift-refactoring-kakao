package gift.service;

import gift.auth.JwtProvider;
import gift.dto.TokenResponse;
import gift.model.Member;
import gift.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    public TokenResponse register(String email, String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        final Member member = memberRepository.save(new Member(email, password));
        final String token = jwtProvider.createToken(member.getEmail());
        return new TokenResponse(token);
    }

    public TokenResponse login(String email, String password) {
        final Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (member.getPassword() == null || !member.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        final String token = jwtProvider.createToken(member.getEmail());
        return new TokenResponse(token);
    }
}
