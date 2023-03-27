package mocacong.server.exception.badrequest;

public class InvalidPhoneException extends BadRequestException {

    public InvalidPhoneException() {
        super("전화번호 형식이 올바르지 않습니다.", 1011);
    }
}
