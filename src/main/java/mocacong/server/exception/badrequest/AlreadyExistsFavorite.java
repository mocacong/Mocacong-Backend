package mocacong.server.exception.badrequest;

public class AlreadyExistsFavorite extends BadRequestException {

    public AlreadyExistsFavorite() {
        super("이미 즐겨찾기된 카페입니다.", 5001);
    }
}
