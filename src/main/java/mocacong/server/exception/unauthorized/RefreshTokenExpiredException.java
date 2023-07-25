package mocacong.server.exception.unauthorized;

import lombok.Getter;

@Getter
public class RefreshTokenExpiredException extends UnauthorizedException {

    public RefreshTokenExpiredException() {
        super("리프레시 토큰이 만료되었습니다. 다시 로그인 해주세요.", 1021);
    }
}
