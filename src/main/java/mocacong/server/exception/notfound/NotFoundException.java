package mocacong.server.exception.notfound;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends RuntimeException {

    private final int httpMethod;

    public NotFoundException() {
        this.httpMethod = HttpStatus.NOT_FOUND.value();
    }
}
