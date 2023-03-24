package mocacong.server.exception.badrequest;

import mocacong.server.exception.InputFieldException;
import org.springframework.http.HttpStatus;

public class IdPasswordMismatchException extends InputFieldException {
    private static final String MESSAGE = "이메일 혹은 비밀번호를 확인해주세요.";

    public IdPasswordMismatchException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST, PASSWORD);
    }
}
