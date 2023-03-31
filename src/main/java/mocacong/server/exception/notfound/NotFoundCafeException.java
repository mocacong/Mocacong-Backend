package mocacong.server.exception.notfound;

public class NotFoundCafeException extends NotFoundException {

    public NotFoundCafeException() {
        super("존재하지 않는 카페입니다.", 2004);
    }
}
