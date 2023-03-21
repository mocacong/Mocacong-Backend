package mocacong.server.exception.notfound;

import lombok.Getter;

@Getter
public class NotFoundMemberException extends NotFoundException {

    public NotFoundMemberException() {
        super("회원이 존재하지 않습니다.");
    }
}
