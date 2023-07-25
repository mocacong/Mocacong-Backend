package mocacong.server.exception.unauthorized;

import lombok.Getter;

@Getter
public class InvalidRefreshTokenException extends UnauthorizedException {

    public InvalidRefreshTokenException() {
        super("올바르지 않은 리프레시 토큰입니다. 다시 로그인해주세요.", 1022);
    }
}
