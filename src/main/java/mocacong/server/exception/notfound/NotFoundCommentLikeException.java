package mocacong.server.exception.notfound;

public class NotFoundCommentLikeException extends NotFoundException {

    public NotFoundCommentLikeException() {
        super("존재하지 않는 댓글 좋아요입니다.", 6001);
    }
}
