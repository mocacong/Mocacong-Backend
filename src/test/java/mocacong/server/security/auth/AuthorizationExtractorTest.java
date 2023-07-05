package mocacong.server.security.auth;

import mocacong.server.exception.unauthorized.InvalidBearerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationExtractorTest {

    @DisplayName("공백 값으로 access token 추출을 시도하면 예외를 반환한다")
    @Test
    void extractAccessTokenByBlankToken() {
        String token = "Bearer ";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", token);

        assertThatThrownBy(() -> AuthorizationExtractor.extractAccessToken(request))
                .isInstanceOf(InvalidBearerException.class);
    }
}