package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidDeskException extends BadRequestException {

    public InvalidDeskException() {
        super("올바르지 않은 책상 정보입니다.");
    }
}
