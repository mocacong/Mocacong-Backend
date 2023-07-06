package mocacong.server.exception.badrequest;

public class BlankTokenException extends BadRequestException {

    public BlankTokenException() {
        super("토큰은 공백일 수 없습니다.", 1018);
    }
}
