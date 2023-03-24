package mocacong.server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static java.lang.Character.LINE_SEPARATOR;

@Getter
public class MocacongException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "일시적으로 접속이 원활하지 않습니다. 찜꽁 서비스 팀에 문의 부탁드립니다." +
            LINE_SEPARATOR +
            "Contact : sunnyk5780@gmail.com / jssung@sk.com";

    private final HttpStatus status;

    public MocacongException() {
        super(DEFAULT_MESSAGE);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public MocacongException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }

    public MocacongException(final String message, final Throwable cause, final HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}
