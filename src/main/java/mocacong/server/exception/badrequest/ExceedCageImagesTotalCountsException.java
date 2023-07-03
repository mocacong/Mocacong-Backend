package mocacong.server.exception.badrequest;

public class ExceedCageImagesTotalCountsException extends BadRequestException{
    public ExceedCageImagesTotalCountsException() {
        super("카페 이미지는 3개 이상 등록하실 수 없습니다.", 2010);
    }
}
