package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidWifiException extends BadRequestException {

    public InvalidWifiException() {
        super("올바르지 않은 와이파이 정보입니다.", 3000);
    }
}
