package mocacong.server.exception.badrequest;

public class InvalidPasswordException extends BadRequestException {

    public InvalidPasswordException() {
        super("비밀번호는 소문자, 숫자를 모두 포함하는 8~20자로 구성되어야 합니다.", 1007);
    }
}
