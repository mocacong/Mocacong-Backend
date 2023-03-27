package mocacong.server.exception.notfound;

import lombok.Getter;
import mocacong.server.exception.MocacongException;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends MocacongException {

    public NotFoundException(String message, int code) {
        super(HttpStatus.NOT_FOUND, message, code);
    }
}
