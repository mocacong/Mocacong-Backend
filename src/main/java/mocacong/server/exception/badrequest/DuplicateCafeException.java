package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class DuplicateCafeException extends BadRequestException {

    public DuplicateCafeException() {
        super("이미 존재하는 카페입니다.", 2009);
    }
}
