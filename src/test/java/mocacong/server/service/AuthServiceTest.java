package mocacong.server.service;

import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import mocacong.server.dto.request.AppleLoginRequest;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.AppleTokenResponse;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.security.auth.apple.ApplePlatformMemberResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    @DisplayName("회원 로그인 요청이 옳다면 토큰을 발급한다")
    void login() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode("a1b2c3d4");
        Member member = new Member("kth990303@naver.com", encodedPassword, "케이", "010-1234-5678");
        memberRepository.save(member);
        AuthLoginRequest loginRequest = new AuthLoginRequest(email, password);

        TokenResponse tokenResponse = authService.login(loginRequest);

        assertNotNull(tokenResponse.getToken());
    }

    @Test
    @DisplayName("회원 로그인 요청이 올바르지 않다면 예외가 발생한다")
    void loginWithException() {
        String email = "kth990303@naver.com";
        String password = "a1b2c3d4";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = new Member(email, encodedPassword, "케이", "010-1234-5678");
        memberRepository.save(member);

        AuthLoginRequest loginRequest = new AuthLoginRequest(email, "wrongPassword");

        assertThrows(PasswordMismatchException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Apple OAuth 로그인 시 가입되지 않은 회원일 경우 이메일 값을 보내고 isRegistered 값을 false로 보낸다")
    void appleOAuthNotRegistered() {
        String expected = "kth@apple.com";
        String platformId = "1234321";
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new ApplePlatformMemberResponse(platformId, expected));

        AppleTokenResponse actual = authService.appleOAuthLogin(new AppleLoginRequest("token"));

        assertAll(
                () -> assertThat(actual.getToken()).isNotNull(),
                () -> assertThat(actual.getEmail()).isEqualTo(expected),
                () -> assertThat(actual.getIsRegistered()).isFalse(),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }

    @Test
    @DisplayName("Apple OAuth 로그인 시 이미 가입된 회원일 경우 토큰과 이메일, 그리고 isRegistered 값을 true로 보낸다")
    void appleOAuthRegistered() {
        String expected = "kth@apple.com";
        String platformId = "1234321";
        Member member = new Member(
                expected,
                passwordEncoder.encode("a1b2c3d4"),
                "케이",
                "010-1234-1234",
                null,
                Platform.APPLE,
                platformId
        );
        memberRepository.save(member);
        when(appleOAuthUserProvider.getApplePlatformMember(anyString()))
                .thenReturn(new ApplePlatformMemberResponse(platformId, expected));

        AppleTokenResponse actual = authService.appleOAuthLogin(new AppleLoginRequest("token"));

        assertAll(
                () -> assertThat(actual.getToken()).isNotNull(),
                () -> assertThat(actual.getEmail()).isEqualTo(expected),
                () -> assertThat(actual.getIsRegistered()).isTrue(),
                () -> assertThat(actual.getPlatformId()).isEqualTo(platformId)
        );
    }
}
