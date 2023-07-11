package mocacong.server.exception.badrequest;

public class InvalidCommentReportException extends BadRequestException {

    public InvalidCommentReportException() {
        super("자신이 작성한 코멘트는 신고할 수 없습니다.", 4006);
    }
}
