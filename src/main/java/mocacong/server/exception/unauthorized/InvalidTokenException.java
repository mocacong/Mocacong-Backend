package mocacong.server.exception.unauthorized;

import mocacong.server.exception.MocacongException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends MocacongException {
    private static final String MESSAGE = "로그인이 필요한 서비스입니다.";

    public InvalidTokenException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED);
    }
}
