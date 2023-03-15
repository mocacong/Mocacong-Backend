package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class DuplicateMemberException extends BadRequestException {

    private final String message;

    public DuplicateMemberException(String message) {
        this.message = message;
    }
}
