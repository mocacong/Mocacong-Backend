package mocacong.server.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import lombok.*;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyCommentCafeResponse {

    private String mapId;
    private String name;
    private String studyType;
    private String roadAddress;
    private List<String> comments;

    public static MyCommentCafeResponse of(Cafe cafe, List<Comment> comments) {
        List<String> contents = comments.stream()
                .map(Comment::getContent)
                .collect(Collectors.toList());
        return new MyCommentCafeResponse(cafe.getMapId(), cafe.getName(), cafe.getStudyType(), cafe.getRoadAddress(), contents);
    }
}
