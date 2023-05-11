package mocacong.server.exception.badrequest;

public class InvalidCommentDeleteException extends BadRequestException {

    public InvalidCommentDeleteException() {
        super("코멘트 삭제는 작성자만 가능합니다.", 4005);
    }
}
