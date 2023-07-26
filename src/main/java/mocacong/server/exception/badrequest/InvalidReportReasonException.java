package mocacong.server.exception.badrequest;

public class InvalidReportReasonException extends BadRequestException {

    public InvalidReportReasonException() {
        super("신고 사유 정보가 올바르지 않습니다.", 7000);
    }
}
