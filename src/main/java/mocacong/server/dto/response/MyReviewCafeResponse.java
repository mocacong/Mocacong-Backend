package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.cafedetail.StudyType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyReviewCafeResponse {

    private String mapId;
    private String name;
    private StudyType studyType;
    private int myScore;
}
