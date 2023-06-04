package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidPowerException extends BadRequestException {

    public InvalidPowerException() {
        super("올바르지 않은 콘센트 정보입니다.", 3007);
    }
}
