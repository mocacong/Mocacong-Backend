package mocacong.server.exception.notfound;

public class NotFoundCommentException extends NotFoundException {

    public NotFoundCommentException() {
        super("존재하지 않는 코멘트입니다.", 4001);
    }
}
