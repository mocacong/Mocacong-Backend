package mocacong.server.exception.badrequest;

public class InvalidCafeImageReportException extends BadRequestException {

    public InvalidCafeImageReportException() {
        super("자신이 등록한 카페 이미지는 신고할 수 없습니다.", 7003);
    }
}
