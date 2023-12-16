package mocacong.server.exception.badrequest;

public class NotExpiredAccessTokenException extends BadRequestException {

    public NotExpiredAccessTokenException() {
        super("아직 만료되지 않은 액세스 토큰입니다", 1022);
    }
}
