package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class DuplicateNicknameException extends BadRequestException {

    public DuplicateNicknameException() {
        super("이미 존재하는 닉네임입니다.", 1004);
    }
}
