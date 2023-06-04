package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidToiletException extends BadRequestException {

    public InvalidToiletException() {
        super("올바르지 않은 화장실 정보입니다.", 3002);
    }
}
