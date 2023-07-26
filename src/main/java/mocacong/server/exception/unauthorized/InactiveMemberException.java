package mocacong.server.exception.unauthorized;

public class InactiveMemberException extends UnauthorizedException {

    public InactiveMemberException() {
        super("서비스에 접근할 수 없는 회원입니다.", 1020);
    }
}
