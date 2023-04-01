package mocacong.server.exception.badrequest;

public class ExceedCommentLengthException extends BadRequestException {

    public ExceedCommentLengthException() {
        super("코멘트 글자 수 제한을 초과했습니다.", 4003);
    }
}
