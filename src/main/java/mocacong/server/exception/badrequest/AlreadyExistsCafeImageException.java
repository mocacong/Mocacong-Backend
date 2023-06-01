package mocacong.server.exception.badrequest;

public class AlreadyExistsCafeImageException extends BadRequestException {

    public AlreadyExistsCafeImageException() {
        super("이미 등록된 카페 이미지입니다.", 3011);
    }
}
