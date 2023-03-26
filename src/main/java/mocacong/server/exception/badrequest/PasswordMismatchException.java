package mocacong.server.exception.badrequest;

import mocacong.server.exception.InputFieldException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends InputFieldException {
    private static final String MESSAGE = "비밀번호가 올바르지 않습니다.";

    public PasswordMismatchException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST, PASSWORD);
    }
}
