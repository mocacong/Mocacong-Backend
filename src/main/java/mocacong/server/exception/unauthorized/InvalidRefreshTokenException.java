package mocacong.server.exception.unauthorized;

import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends UnauthorizedException {

    public InvalidRefreshTokenException() {
        super("올바르지 않은 Refresh Token 입니다. 다시 로그인해주세요.", 1021);
    }
}
