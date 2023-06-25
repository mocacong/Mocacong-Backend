package mocacong.server.exception.badrequest;

public class InvalidFileEmptyException extends BadRequestException {

    public InvalidFileEmptyException() {
        super("빈 파일은 업로드할 수 없습니다.", 9007);
    }
}
