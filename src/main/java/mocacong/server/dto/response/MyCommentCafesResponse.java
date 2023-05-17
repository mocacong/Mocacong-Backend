package mocacong.server.dto.response;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyCommentCafesResponse {

    private Boolean isEnd;
    private List<MyCommentCafeResponse> cafes;
}
