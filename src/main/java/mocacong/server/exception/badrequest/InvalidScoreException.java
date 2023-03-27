package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class InvalidScoreException extends BadRequestException {

    public InvalidScoreException() {
        super("범위에서 벗어난 평점입니다.", 2002);
    }
}
