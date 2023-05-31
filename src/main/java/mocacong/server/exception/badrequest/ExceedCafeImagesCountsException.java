package mocacong.server.exception.badrequest;

import lombok.Getter;

@Getter
public class ExceedCafeImagesCountsException extends BadRequestException {

    public ExceedCafeImagesCountsException() {
        super("카페 이미지는 한 번에 최대 3개까지만 업로드 가능합니다.", 2008);
    }
}
