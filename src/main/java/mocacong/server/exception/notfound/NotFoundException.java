package mocacong.server.exception.notfound;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends RuntimeException {

    private final int httpMethod;
    private final String message;

    public NotFoundException(String message) {
        this.httpMethod = HttpStatus.NOT_FOUND.value();
        this.message = message;
    }
}
