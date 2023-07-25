package mocacong.server.exception.unauthorized;

import lombok.Getter;

@Getter
public class AccessTokenExpiredException extends UnauthorizedException {

    public AccessTokenExpiredException() {
        super("로그인 인증 유효기간이 만료되었습니다. 다시 로그인 해주세요.", 1014);
    }

    public AccessTokenExpiredException(String message) {
        super(message, 1014);
    }
}
