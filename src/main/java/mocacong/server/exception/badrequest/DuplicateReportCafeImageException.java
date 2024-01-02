package mocacong.server.exception.badrequest;

public class DuplicateReportCafeImageException extends BadRequestException {

    public DuplicateReportCafeImageException() {
        super("이미 신고한 카페 이미지입니다.", 7004);
    }
}
