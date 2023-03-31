package mocacong.server.exception.badrequest;

public class AlreadyExistsCafeReview extends BadRequestException {

    public AlreadyExistsCafeReview() {
        super("이미 리뷰를 등록했습니다.", 3010);
    }
}
