package mocacong.server.exception.notfound;

import lombok.Getter;

@Getter
public class NotFoundMemberException extends NotFoundException {

    private final String message;

    public NotFoundMemberException(String message) {
        this.message = message;
    }
}
