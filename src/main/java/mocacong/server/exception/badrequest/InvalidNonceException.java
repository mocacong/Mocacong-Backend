package mocacong.server.exception.badrequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class InvalidNonceException extends BadRequestException {

    @Value("${mocacong.nonce}")
    private String nonce;

    public InvalidNonceException() {
        super("nonce 값이 다른 올바르지 않은 요청입니다.", 1016);
    }

    public InvalidNonceException(String requestNonce) {
        super("nonce 값이 다른 올바르지 않은 요청입니다.", 1016);
        log.info("request = {}, actual = {}", requestNonce, nonce);
    }
}
