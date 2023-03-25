package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidStudyTypeException extends BadRequestException {

    public InvalidStudyTypeException() {
        super("올바르지 않은 스터디 타입 정보입니다.");
    }
}
