package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.cafedetail.StudyType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MyReviewCafeResponse {

    private String mapId;
    private String name;
    private String studyType;
    private int myScore;

    public MyReviewCafeResponse(String mapId, String name, StudyType studyType, int myScore) {
        this.mapId = mapId;
        this.name = name;
        this.studyType = studyType.getValue();
        this.myScore = myScore;
    }
}
