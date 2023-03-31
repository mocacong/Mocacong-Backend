package mocacong.server.exception.notfound;

public class NotFoundReviewException extends NotFoundException {

    public NotFoundReviewException() {
        super("해당 카페에 기여한 평점이 존재하지 않습니다.", 2006);
    }
}
