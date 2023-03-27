package mocacong.server.exception.unauthorized;

import mocacong.server.exception.MocacongException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends MocacongException {

    public UnauthorizedException(String message, int code) {
        super(HttpStatus.UNAUTHORIZED, message, code);
    }
}
