package mocacong.server.exception.badrequest;

public class InvalidArgumentException extends BadRequestException {

    public InvalidArgumentException() {
        super("공백일 수 없습니다.", 1012);
    }
}
