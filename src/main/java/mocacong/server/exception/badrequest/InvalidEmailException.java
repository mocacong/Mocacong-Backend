package mocacong.server.exception.badrequest;

public class InvalidEmailException extends BadRequestException {

    public InvalidEmailException() {
        super("이메일 형식이 올바르지 않습니다.", 1006);
    }
}
