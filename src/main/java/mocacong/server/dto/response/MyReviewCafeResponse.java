package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyReviewCafeResponse {

    private String mapId;
    private String name;
    private int myScore;
}
