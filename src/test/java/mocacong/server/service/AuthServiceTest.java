package mocacong.server.service;

import groovy.util.logging.Slf4j;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.domain.Status;
import mocacong.server.domain.Token;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.request.RefreshTokenRequest;
import mocacong.server.dto.response.OAuthTokenResponse;
import mocacong.server.dto.response.ReissueTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.NotExpiredAccessTokenException;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.exception.unauthorized.InactiveMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@ServiceTest
class AuthServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @MockBean
    private AppleOAuthUserProvider appleOAuthUserProvider;
    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("회원 로그인 요청이 옳다면 토큰을 발급하고 상태는 ACTIVE로 반환한다")
    void login() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이");
        memberRepository.save(member);
        AuthLoginRequest loginRequest = new AuthLoginRequest(email, password);

        TokenResponse tokenResponse = authService.login(loginRequest);

        assertAll(
                () -> assertThat(member.getStatus()).isEqualTo(Status.ACTIVE),
                () -> assertNotNull(tokenResponse.getAccessToken()),
                () -> assertNotNull(tokenResponse.getRefreshToken()),
                () -> assertThat(tokenResponse.getUserReportCount()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("status가 INACTIVE인 회원이 자체 로그인 시도할 시 예외를 반환한다")
    void loginWithInActive() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이", null,
                Platform.MOCACONG, "111111", Status.INACTIVE);
        memberRepository.save(member);
        AuthLoginRequest loginRequest = new AuthLoginRequest(email, password);

        assertThrows(InactiveMemberException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("회원 로그인 요청이 올바르지 않다면 예외가 발생한다")
    void loginWithException() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = new Member(email, encodedPassword, "케이");
        memberRepository.save(member);

        AuthLoginRequest loginRequest = new AuthLoginRequest(email, "wrongPassword");

        assertThrows(PasswordMismatchException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("OAuth 로그인 시 가입되지 않은 회원일 경우 이메일 값을 보내고 isRegistered 값을 false로 보낸다")
    void loginOAuthNotRegistered() {
        String expected = "kth@apple.com";
        String platformId = "1234321";
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new OAuthPlatformMemberResponse(platformId, expected));

        OAuthTokenResponse actual = authService.appleOAuthLogin(new AppleLoginRequest("token"));

        assertAll(
                () -> assertThat(actual.getAccessToken()).isNotNull(),
                () -> assertThat(actual.getRefreshToken()).isNotNull(),
                () -> assertThat(actual.getEmail()).isEqualTo(expected),
                () -> assertThat(actual.getIsRegistered()).isFalse(),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }

    @Test
    @DisplayName("OAuth 로그인 시 이미 가입된 회원일 경우 토큰과 이메일, 그리고 isRegistered 값을 true로 보낸다")
    void loginOAuthRegisteredAndMocacongMember() {
        String expected = "kth@apple.com";
        String platformId = "1234321";
        Member member = new Member(
                expected,
                passwordEncoder.encode("a1b2c3d4"),
                "케이",
                null,
                Platform.APPLE,
                platformId
        );
        memberRepository.save(member);
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new OAuthPlatformMemberResponse(platformId, expected));

        OAuthTokenResponse actual = authService.appleOAuthLogin(new AppleLoginRequest("token"));

        assertAll(
                () -> assertThat(actual.getAccessToken()).isNotNull(),
                () -> assertThat(actual.getRefreshToken()).isNotNull(),
                () -> assertThat(actual.getEmail()).isEqualTo(expected),
                () -> assertThat(actual.getIsRegistered()).isTrue(),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }

    @Test
    @DisplayName("OAuth 로그인 시 등록은 완료됐지만 회원가입 절차에 실패해 닉네임이 없으면 isRegistered 값을 false로 보낸다")
    void loginOAuthRegisteredButNotMocacongMember() {
        String expected = "kth@apple.com";
        String platformId = "1234321";
        Member member = new Member("kth@apple.com", Platform.APPLE, "1234321");
        memberRepository.save(member);
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new OAuthPlatformMemberResponse(platformId, expected));

        OAuthTokenResponse actual = authService.appleOAuthLogin(new AppleLoginRequest("token"));

        assertAll(
                () -> assertThat(actual.getAccessToken()).isNotNull(),
                () -> assertThat(actual.getRefreshToken()).isNotNull(),
                () -> assertThat(actual.getEmail()).isEqualTo(expected),
                () -> assertThat(actual.getIsRegistered()).isFalse(),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }

    @Test
    @DisplayName("자체 회원가입을 진행한 이메일로 정상적으로 OAuth 로그인을 한다")
    void loginOAuthWithMocacongEmail() {
        String email = "kth@apple.com";
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member(email, encodedPassword, "케이");
        memberRepository.save(member);
        String platformId = "1234321";
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new OAuthPlatformMemberResponse(platformId, email));

        OAuthTokenResponse actual = authService.appleOAuthLogin(new AppleLoginRequest("token"));

        assertAll(
                () -> assertThat(actual.getAccessToken()).isNotNull(),
                () -> assertThat(actual.getRefreshToken()).isNotNull(),
                () -> assertThat(actual.getEmail()).isEqualTo(email),
                () -> assertThat(actual.getIsRegistered()).isFalse(),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }

    @Test
    @DisplayName("OAuth 로그인을 진행한 이메일로 자체 회원가입을 한다")
    void signUpWithAppleEmail() {
        String email = "kth@apple.com";
        String platformId = "1234321";
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new OAuthPlatformMemberResponse(platformId, email));
        OAuthTokenResponse response = authService.appleOAuthLogin(new AppleLoginRequest("token"));
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");

        Member member = new Member(email, encodedPassword, "케이");
        memberRepository.save(member);

        assertAll(
                () -> assertThat(response.getAccessToken()).isNotNull(),
                () -> assertThat(response.getRefreshToken()).isNotNull(),
                () -> assertThat(response.getEmail()).isEqualTo(member.getEmail())
        );
    }

    @Test
    @DisplayName("status가 INACTIVE인 회원이 OAuth 로그인 시도할 시 예외를 반환한다")
    void loginOAuthWithInactive() {
        String expected = "kth@apple.com";
        String platformId = "1234321";
        Member member = new Member(
                expected,
                passwordEncoder.encode("a1b2c3d4"),
                "케이",
                null,
                Platform.APPLE,
                platformId,
                Status.INACTIVE
        );
        memberRepository.save(member);
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new OAuthPlatformMemberResponse(platformId, expected));

        assertThrows(InactiveMemberException.class,
                () -> authService.appleOAuthLogin(new AppleLoginRequest("token")));
    }

    @Test
    @DisplayName("액세스 토큰 재발급 요청이 올바르다면 액세스 토큰을 재발급한다")
    void reissueAccessToken() {
        String refreshToken = "valid-refresh-token";
        Date now = new Date();
        long expiredValidityInMilliseconds = 0L;
        String expiredAccessToken = Jwts.builder()
                .setExpiration(new Date(now.getTime() + expiredValidityInMilliseconds))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이");

        Token token = new Token(member.getId(), refreshToken, expiredAccessToken, 0);
        when(refreshTokenService.getMemberFromRefreshToken(refreshToken)).thenReturn(member);
        when(refreshTokenService.findTokenByRefreshToken(refreshToken)).thenReturn(token);

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        ReissueTokenResponse response = authService.reissueAccessToken(request);

        Assertions.assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(member.getReportCount(), response.getUserReportCount())
        );
    }

    @Test
    @DisplayName("만료되지 않은 액세스 토큰을 가지고 재발급 요청 시 예외 발생")
    void reissueAccessTokenFailsWhenNotExpired() {
        String refreshToken = "valid-refresh-token";
        Date now = new Date();
        long futureValidityInMilliseconds = 3600000L;
        String validAccessToken = Jwts.builder()
                .setExpiration(new Date(now.getTime() + futureValidityInMilliseconds))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이");

        Token token = new Token(member.getId(), refreshToken, validAccessToken, 999);
        when(refreshTokenService.getMemberFromRefreshToken(refreshToken)).thenReturn(member);
        when(refreshTokenService.findTokenByRefreshToken(refreshToken)).thenReturn(token);

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        assertThrows(NotExpiredAccessTokenException.class,
                () -> authService.reissueAccessToken(request));
    }
}
