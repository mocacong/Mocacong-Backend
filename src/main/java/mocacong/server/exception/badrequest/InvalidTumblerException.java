package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidTumblerException extends BadRequestException {

    public InvalidTumblerException() {
        super("올바르지 않은 텀블러 정보입니다.");
    }
}
