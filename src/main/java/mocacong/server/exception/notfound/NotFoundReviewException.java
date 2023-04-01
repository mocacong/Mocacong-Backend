package mocacong.server.exception.notfound;

public class NotFoundReviewException extends NotFoundException {

    public NotFoundReviewException() {
        super("존재하지 않는 리뷰입니다.", 3008);
    }
}
