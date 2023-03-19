package mocacong.server.exception.badrequest;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequestException extends RuntimeException {

    private final int httpMethod;

    public BadRequestException() {
        this.httpMethod = HttpStatus.BAD_REQUEST.value();
    }
}
