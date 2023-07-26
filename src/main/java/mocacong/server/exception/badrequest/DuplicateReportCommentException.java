package mocacong.server.exception.badrequest;

public class DuplicateReportCommentException extends BadRequestException {

    public DuplicateReportCommentException() {
        super("이미 신고한 코멘트입니다.", 7002);
    }
}
