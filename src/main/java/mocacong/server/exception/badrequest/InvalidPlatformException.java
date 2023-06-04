package mocacong.server.exception.badrequest;

public class InvalidPlatformException extends BadRequestException {

    public InvalidPlatformException() {
        super("플랫폼 정보가 올바르지 않습니다.", 1010);
    }
}
