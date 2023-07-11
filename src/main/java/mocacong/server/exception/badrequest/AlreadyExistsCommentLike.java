package mocacong.server.exception.badrequest;

public class AlreadyExistsCommentLike extends BadRequestException {

    public AlreadyExistsCommentLike() {
        super("이미 좋아요한 댓글입니다.", 6002);
    }
}
