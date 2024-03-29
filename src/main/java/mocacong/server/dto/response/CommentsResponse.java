package mocacong.server.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class CommentsResponse {

    private Boolean isEnd;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long count;
    private List<CommentResponse> comments;

    public CommentsResponse(Boolean isEnd, List<CommentResponse> comments) {
        this.isEnd = isEnd;
        this.comments = comments;
    }
}
