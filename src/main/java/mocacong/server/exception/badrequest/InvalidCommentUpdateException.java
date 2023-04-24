package mocacong.server.exception.badrequest;

public class InvalidCommentUpdateException extends BadRequestException {

    public InvalidCommentUpdateException() {
        super("코멘트 수정은 작성자만 가능합니다.", 4004);
    }
}
