package mocacong.server.exception.badrequest;

public class InvalidCommentLikeException extends BadRequestException {
    public InvalidCommentLikeException() {
        super("자신의 댓글에 좋아요할 수 없습니다.", 6002);
    }
}
