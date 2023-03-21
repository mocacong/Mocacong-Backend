package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class DuplicateMemberException extends BadRequestException {

    public DuplicateMemberException() {
        super("이미 존재하는 회원입니다.");
    }
}
