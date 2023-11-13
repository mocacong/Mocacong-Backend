package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.domain.Status;
import mocacong.server.domain.Token;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.request.KakaoLoginRequest;
import mocacong.server.dto.request.RefreshTokenRequest;
import mocacong.server.dto.response.OAuthTokenResponse;
import mocacong.server.dto.response.ReissueTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.NotExpiredAccessTokenException;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.exception.unauthorized.InactiveMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.security.auth.kakao.KakaoOAuthUserProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AppleOAuthUserProvider appleOAuthUserProvider;
    private final KakaoOAuthUserProvider kakaoOAuthUserProvider;
    private final RedisTemplate<String, Token> redisTemplate;

    public TokenResponse login(AuthLoginRequest request) {
        Member findMember = memberRepository.findByEmailAndPlatform(request.getEmail(), Platform.MOCACONG)
                .orElseThrow(NotFoundMemberException::new);
        validatePassword(findMember, request.getPassword());
        validateStatus(findMember);

        String accessToken = issueAccessToken(findMember);
        String refreshToken = issueRefreshToken();

        // Redis에 refresh 토큰 저장 (사용자 기본키 Id, refresh 토큰, access 토큰)
        refreshTokenService.saveTokenInfo(findMember.getId(), refreshToken, accessToken);
        int userReportCount = findMember.getReportCount();

        return TokenResponse.from(accessToken, refreshToken, userReportCount);
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
                    validateStatus(findMember);
                    int userReportCount = findMember.getReportCount();
                    String accessToken = issueAccessToken(findMember);
                    String refreshToken = issueRefreshToken();

                    refreshTokenService.saveTokenInfo(findMember.getId(), refreshToken, accessToken);

                    // OAuth 로그인은 성공했지만 회원가입에 실패한 경우
                    if (!findMember.isRegisteredOAuthMember()) {
                        return new OAuthTokenResponse(accessToken, refreshToken, findMember.getEmail(),
                                false, platformId, userReportCount);
                    }
                    return new OAuthTokenResponse(accessToken, refreshToken, findMember.getEmail(),
                            true, platformId, userReportCount);
                })
                .orElseGet(() -> {
                    Member oauthMember = new Member(email, platform, platformId, Status.ACTIVE);
                    Member savedMember = memberRepository.save(oauthMember);
                    String accessToken = issueAccessToken(savedMember);
                    String refreshToken = issueRefreshToken();

                    refreshTokenService.saveTokenInfo(savedMember.getId(), refreshToken, accessToken);
                    return new OAuthTokenResponse(accessToken, refreshToken, email, false, platformId,
                            savedMember.getReportCount());
                });
    }

    private String issueAccessToken(final Member findMember) {
        return jwtTokenProvider.createAccessToken(findMember.getId());
    }

    private String issueRefreshToken() {
        return refreshTokenService.createRefreshToken();
    }

    private void validatePassword(final Member findMember, final String password) {
        if (!passwordEncoder.matches(password, findMember.getPassword())) {
            throw new PasswordMismatchException();
        }
    }

    private void validateStatus(final Member findMember) {
        if (findMember.getStatus() != Status.ACTIVE) {
            throw new InactiveMemberException();
        }
    }

    @Transactional
    public ReissueTokenResponse reissueAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        Member member = refreshTokenService.validateRefreshTokenAndGetMember(refreshToken);
        Token token = refreshTokenService.findTokenByRefreshToken(refreshToken);
        String oldAccessToken = token.getAccessToken();

        // 이전에 발급된 액세스 토큰이 만료가 되어야 새로운 액세스 토큰 발급
        if (jwtTokenProvider.validateIsExpiredAccessToken(oldAccessToken)) {
            String newAccessToken = jwtTokenProvider.createAccessToken(member.getId());
            token.setAccessToken(newAccessToken);
            redisTemplate.opsForValue().set(refreshToken, token, token.getExpiration()-14400, TimeUnit.SECONDS);
            return ReissueTokenResponse.from(newAccessToken, member.getReportCount());
        } else {
            throw new NotExpiredAccessTokenException();
        }
    }
}
