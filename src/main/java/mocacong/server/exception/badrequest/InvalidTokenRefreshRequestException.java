package mocacong.server.exception.badrequest;

public class InvalidTokenRefreshRequestException extends BadRequestException {
    public InvalidTokenRefreshRequestException() {
        super("올바르지 않은 토큰 재발급 요청입니다.", 1023);
    }
}
