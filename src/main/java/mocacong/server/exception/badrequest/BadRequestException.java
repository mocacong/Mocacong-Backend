package mocacong.server.exception.badrequest;

import lombok.Getter;
import mocacong.server.exception.MocacongException;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends MocacongException {

    public BadRequestException(String message, int code) {
        super(HttpStatus.BAD_REQUEST, message, code);
    }
}
