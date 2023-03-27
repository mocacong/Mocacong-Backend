package mocacong.server.controller;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.dto.response.ErrorResponse;
import mocacong.server.exception.MocacongException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {

    private static final int FIELD_ERROR_CODE_INDEX = 0;
    private static final int FIELD_ERROR_MESSAGE_INDEX = 1;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInputFieldException(MethodArgumentNotValidException e) {
        FieldError mainError = e.getFieldErrors().get(0);
        String[] errorInfo = Objects.requireNonNull(mainError.getDefaultMessage()).split(":");

        int code = Integer.parseInt(errorInfo[FIELD_ERROR_CODE_INDEX]);
        String message = errorInfo[FIELD_ERROR_MESSAGE_INDEX];

        return ResponseEntity.badRequest().body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(MocacongException.class)
    public ResponseEntity<ErrorResponse> handleMocacongException(MocacongException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unhandledException(Exception e, HttpServletRequest request) {
        log.error("UnhandledException: {} {} errMessage={}\n",
                request.getMethod(),
                request.getRequestURI(),
                e.getMessage()
        );
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(9999, "일시적으로 접속이 원활하지 않습니다. 모카콩 서비스 팀에 문의 부탁드립니다."));
    }
}
