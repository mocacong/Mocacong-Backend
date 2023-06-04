package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidDeskException extends BadRequestException {

    public InvalidDeskException() {
        super("올바르지 않은 테이블 정보입니다.", 3004);
    }
}
