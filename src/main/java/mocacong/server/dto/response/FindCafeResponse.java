package mocacong.server.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FindCafeResponse {

    private Boolean favorite;
    private Long favoriteId;
    private double score;
    private Integer myScore;
    private String studyType;
    private int reviewsCount;
    private List<ReviewResponse> reviews;
    private int commentsCount;
    private List<CommentResponse> comments;
}
