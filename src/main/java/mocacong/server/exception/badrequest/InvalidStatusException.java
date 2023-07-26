package mocacong.server.exception.badrequest;

public class InvalidStatusException extends BadRequestException {

    public InvalidStatusException() {
        super("회원 상태 정보가 올바르지 않습니다.", 1019);
    }
}
