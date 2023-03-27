package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidParkingException extends BadRequestException {

    public InvalidParkingException() {
        super("올바르지 않은 주차 정보입니다.", 3001);
    }
}
