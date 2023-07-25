package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.domain.Status;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.OAuthTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.exception.unauthorized.InactiveMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ServiceTest
class AuthServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthService authService;

    @MockBean
    private AppleOAuthUserProvider appleOAuthUserProvider;

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
}
