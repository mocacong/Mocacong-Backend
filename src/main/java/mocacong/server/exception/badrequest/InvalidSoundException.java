package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidSoundException extends BadRequestException {

    public InvalidSoundException() {
        super("올바르지 않은 분위기 정보입니다.");
    }
}
