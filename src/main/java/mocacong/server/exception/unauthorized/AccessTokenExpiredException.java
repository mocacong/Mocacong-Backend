package mocacong.server.exception.unauthorized;

import lombok.Getter;

@Getter
public class AccessTokenExpiredException extends UnauthorizedException {

    public AccessTokenExpiredException() {
        super("Access Token 유효기간이 만료되었습니다.", 1014);
    }

    public AccessTokenExpiredException(String message) {
        super(message, 1014);
    }
}
