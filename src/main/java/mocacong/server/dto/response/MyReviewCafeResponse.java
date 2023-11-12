package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.cafedetail.StudyType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MyReviewCafeResponse {

    private String mapId;
    private String name;
    private String myStudyType;
    private int myScore;
    private String roadAddress;

    public MyReviewCafeResponse(String mapId, String name, StudyType myStudyType, int myScore, String roadAddress) {
        this.mapId = mapId;
        this.name = name;
        this.myStudyType = myStudyType.toString();
        this.myScore = myScore;
        this.roadAddress = roadAddress;
    }
}
