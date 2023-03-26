package mocacong.server.exception.unauthorized;

import mocacong.server.exception.MocacongException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends MocacongException {
    private static final String MESSAGE = "로그인 인증 유효기간이 만료되었습니다. 다시 로그인 해주세요.";

    public TokenExpiredException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED);
    }
}
