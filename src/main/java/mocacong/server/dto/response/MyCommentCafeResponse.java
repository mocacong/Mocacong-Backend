package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Comment;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyCommentCafeResponse {

    private String mapId;
    private String name;
    private String studyType;
    private String roadAddress;
    private double score;
    private List<String> commentContents;

    public static MyCommentCafeResponse of(Cafe cafe, List<Comment> comments) {
        List<String> contents = comments.stream()
                .map(Comment::getContent)
                .collect(Collectors.toList());
        return new MyCommentCafeResponse(cafe.getMapId(), cafe.getName(), cafe.getStudyType(), cafe.getRoadAddress(),
                cafe.findAverageScore(),  contents);
    }
}
