package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidStudyTypeException extends BadRequestException {

    public InvalidStudyTypeException() {
        super("올바르지 않은 StudyType 정보입니다.", 3005);
    }
}
