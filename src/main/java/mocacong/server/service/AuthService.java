package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.AppleTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.security.auth.apple.ApplePlatformMemberResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AppleOAuthUserProvider appleOAuthUserProvider;

    public TokenResponse login(AuthLoginRequest request) {
        Member findMember = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(NotFoundMemberException::new);
        validatePassword(findMember, request.getPassword());

        String token = issueToken(findMember);
        return TokenResponse.from(token);
    }

    public AppleTokenResponse appleOAuthLogin(AppleLoginRequest request) {
        ApplePlatformMemberResponse applePlatformMember =
                appleOAuthUserProvider.getApplePlatformMember(request.getToken());
        String platformId = applePlatformMember.getPlatformId();

        return memberRepository.findIdByPlatformAndPlatformId(Platform.APPLE, platformId)
                .map(memberId -> {
                    Member findMember = memberRepository.findById(memberId)
                            .orElseThrow(NotFoundMemberException::new);
                    String token = issueToken(findMember);

                    // OAuth 로그인은 성공했지만 회원가입에 실패한 경우
                    if (!findMember.isRegisteredOAuthMember()) {
                        return new AppleTokenResponse(token, findMember.getEmail(), false, platformId);
                    }

                    return new AppleTokenResponse(token, findMember.getEmail(), true, platformId);
                })
                .orElseGet(() -> {
                    Member oauthMember = new Member(applePlatformMember.getEmail(), Platform.APPLE, platformId);
                    Member savedMember = memberRepository.save(oauthMember);
                    String token = issueToken(savedMember);
                    return new AppleTokenResponse(token, applePlatformMember.getEmail(), false, platformId);
                });
    }

    private String issueToken(final Member findMember) {
        String email = findMember.getEmail();

        return jwtTokenProvider.createToken(email);
    }

    private void validatePassword(final Member findMember, final String password) {
        if (!passwordEncoder.matches(password, findMember.getPassword())) {
            throw new PasswordMismatchException();
        }
    }
}
