package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.domain.Status;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.request.KakaoLoginRequest;
import mocacong.server.dto.response.OAuthTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.unauthorized.InactiveMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.security.auth.kakao.KakaoOAuthUserProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AppleOAuthUserProvider appleOAuthUserProvider;
    private final KakaoOAuthUserProvider kakaoOAuthUserProvider;

    public TokenResponse login(AuthLoginRequest request) {
        Member findMember = memberRepository.findByEmailAndPlatform(request.getEmail(), Platform.MOCACONG)
                .orElseThrow(NotFoundMemberException::new);
        validatePassword(findMember, request.getPassword());
        validateStatus(findMember);

        String token = issueToken(findMember);
        Status status = findMember.getStatus();

        return TokenResponse.from(token, status);
    }

    public OAuthTokenResponse appleOAuthLogin(AppleLoginRequest request) {
        OAuthPlatformMemberResponse applePlatformMember =
                appleOAuthUserProvider.getApplePlatformMember(request.getToken());
        return generateOAuthTokenResponse(
                Platform.APPLE,
                applePlatformMember.getEmail(),
                applePlatformMember.getPlatformId()
        );
    }

    public OAuthTokenResponse kakaoOAuthLogin(KakaoLoginRequest request) {
        OAuthPlatformMemberResponse kakaoPlatformMember =
                kakaoOAuthUserProvider.getKakaoPlatformMember(request.getCode());
        return generateOAuthTokenResponse(
                Platform.KAKAO,
                kakaoPlatformMember.getEmail(),
                kakaoPlatformMember.getPlatformId()
        );
    }

    private OAuthTokenResponse generateOAuthTokenResponse(Platform platform, String email, String platformId) {
        return memberRepository.findIdByPlatformAndPlatformId(platform, platformId)
                .map(memberId -> {
                    Member findMember = memberRepository.findById(memberId)
                            .orElseThrow(NotFoundMemberException::new);
                    String token = issueToken(findMember);
                    // OAuth 로그인은 성공했지만 회원가입에 실패한 경우
                    if (!findMember.isRegisteredOAuthMember()) {
                        return new OAuthTokenResponse(token, findMember.getEmail(), false, platformId);
                    }
                    return new OAuthTokenResponse(token, findMember.getEmail(), true, platformId);
                })
                .orElseGet(() -> {
                    Member oauthMember = new Member(email, platform, platformId);
                    Member savedMember = memberRepository.save(oauthMember);
                    String token = issueToken(savedMember);
                    return new OAuthTokenResponse(token, email, false, platformId);
                });
    }

    private String issueToken(final Member findMember) {
        return jwtTokenProvider.createToken(findMember.getId());
    }

    private void validatePassword(final Member findMember, final String password) {
        if (!passwordEncoder.matches(password, findMember.getPassword())) {
            throw new PasswordMismatchException();
        }
    }

    private void validateStatus(final Member findMember) {
        if (findMember.getStatus() == Status.INACTIVE) {
            throw new InactiveMemberException();
        }
    }
}
