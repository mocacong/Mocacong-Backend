package mocacong.server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InputFieldException extends MocacongException {
    protected static final String EMAIL = "email";
    protected static final String PASSWORD = "password";
    protected static final String NICKNAME = "nickname";
    protected static final String PHONE = "phone";

    private final String field;

    public InputFieldException(final String message, final HttpStatus status, final String field) {
        super(message, status);
        this.field = field;
    }
}
