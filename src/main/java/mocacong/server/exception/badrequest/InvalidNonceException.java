package mocacong.server.exception.badrequest;

public class InvalidNonceException extends BadRequestException {

    public InvalidNonceException() {
        super("nonce 값이 다른 올바르지 않은 요청입니다.", 1016);
    }
}
