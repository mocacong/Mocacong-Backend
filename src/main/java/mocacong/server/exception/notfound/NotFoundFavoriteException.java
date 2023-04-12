package mocacong.server.exception.notfound;

public class NotFoundFavoriteException extends NotFoundException {

    public NotFoundFavoriteException() {
        super("존재하지 않는 즐겨찾기입니다.", 5000);
    }
}
