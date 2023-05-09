package mocacong.server.exception.notfound;

public class NotFoundCafeImageException extends NotFoundException {

    public NotFoundCafeImageException() {
        super("존재하지 않는 카페 이미지입니다.", 2007);
    }
}
