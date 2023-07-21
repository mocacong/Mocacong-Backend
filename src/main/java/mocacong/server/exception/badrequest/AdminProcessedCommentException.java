package mocacong.server.exception.badrequest;

public class AdminProcessedCommentException extends BadRequestException{
    public AdminProcessedCommentException() {
        super("관리자에 의해 처리된 코멘트입니다.", 7003);
    }
}
