package mocacong.server.exception.notfound;

import lombok.Getter;

@Getter
public class NotFoundMemberException extends NotFoundException {

    public NotFoundMemberException() {
        super("존재하지 않는 회원입니다.", 1001);
    }
}
