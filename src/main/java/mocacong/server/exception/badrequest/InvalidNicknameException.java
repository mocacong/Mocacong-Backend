package mocacong.server.exception.badrequest;

public class InvalidNicknameException extends BadRequestException {

    public InvalidNicknameException() {
        super("닉네임은 영어, 한글로만 구성된 2~6자여야 합니다.", 1009);
    }
}
