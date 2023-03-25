package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidScoreException extends BadRequestException {

    public InvalidScoreException() {
        super("별점은 1점 이상 5점 이하여야 합니다.");
    }
}
