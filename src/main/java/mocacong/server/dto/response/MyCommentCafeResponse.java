package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyCommentCafeResponse {

    private String mapId;
    private String name;
    private String studyType;
    private String comment;
    private String roadAddress;

    public static MyCommentCafeResponse from(Comment comment) {
        return new MyCommentCafeResponse(
                comment.getCafe().getMapId(),
                comment.getCafe().getName(),
                comment.getCafe().getStudyType(),
                comment.getContent(),
                comment.getCafe().getRoadAddress()
        );
    }
}
