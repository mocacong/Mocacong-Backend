package mocacong.server.controller;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.dto.response.ErrorResponse;
import mocacong.server.exception.MocacongException;
import mocacong.server.support.SlackAlarmGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ControllerAdvice {

    private static final int FIELD_ERROR_CODE_INDEX = 0;
    private static final int FIELD_ERROR_MESSAGE_INDEX = 1;

    private final SlackAlarmGenerator slackAlarmGenerator;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInputFieldException(MethodArgumentNotValidException e) {
        FieldError mainError = e.getFieldErrors().get(0);
        String[] errorInfo = Objects.requireNonNull(mainError.getDefaultMessage()).split(":");

        int code = Integer.parseInt(errorInfo[FIELD_ERROR_CODE_INDEX]);
        String message = errorInfo[FIELD_ERROR_MESSAGE_INDEX];

        return ResponseEntity.badRequest().body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(HttpMessageNotReadableException e) {
        log.warn("Json Exception ErrMessage={}\n", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(9000, "Json 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(HttpMediaTypeException.class)
    public ResponseEntity<ErrorResponse> handleContentTypeException(HttpMediaTypeException e) {
        log.warn("ContentType Exception ErrMessage={}\n", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(9001, "ContentType 값이 올바르지 않습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleRequestMethodException(HttpRequestMethodNotSupportedException e) {
        log.warn("Http Method not supported Exception ErrMessage={}\n", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(9002, "해당 Http Method에 맞는 API가 존재하지 않습니다."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeLimitExceeded(MultipartException e) {
        log.error("File Size Limit Exception ErrMessage={}\n", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(9003, "이미지 용량이 10MB를 초과합니다."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParamException(MissingServletRequestParameterException e) {
        log.warn("Request Param is Missing! ErrMessage={}\n", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(9004, "요청 param 이름이 올바르지 않습니다."));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingMultiPartParamException(MissingServletRequestPartException e) {
        log.warn("MultipartFile Request Param is Missing! ErrMessage={}\n", e.getMessage());

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(9005, "요청 MultipartFile param 이름이 올바르지 않습니다."));
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
        slackAlarmGenerator.sendSlackAlertErrorLog(e, request);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(9999, "일시적으로 접속이 원활하지 않습니다. 모카콩 서비스 팀에 문의 부탁드립니다."));
    }
}
