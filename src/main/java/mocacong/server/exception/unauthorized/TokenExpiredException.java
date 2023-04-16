package mocacong.server.exception.unauthorized;

public class TokenExpiredException extends UnauthorizedException {

    public TokenExpiredException() {
        super("로그인 인증 유효기간이 만료되었습니다. 다시 로그인 해주세요.", 1014);
    }

    public TokenExpiredException(String message) {
        super(message, 1014);
    }
}
