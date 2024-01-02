package mocacong.server.exception.unauthorized;

public class InvalidAccessTokenException extends UnauthorizedException {

    public InvalidAccessTokenException() {
        super("올바르지 않은 Access Token 입니다. 다시 로그인해주세요.", 1015);
    }

    public InvalidAccessTokenException(String message) {
        super(message, 1015);
    }
}
