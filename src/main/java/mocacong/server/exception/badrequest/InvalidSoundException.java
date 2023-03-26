package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidSoundException extends BadRequestException {

    public InvalidSoundException() {
        super("올바르지 않은 소란도 정보입니다.", 3006);
    }
}
