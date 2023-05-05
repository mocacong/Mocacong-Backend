package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.CafeDetail;
import mocacong.server.domain.Review;
import mocacong.server.domain.Score;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeMyReviewResponse {

    private Integer score;
    private String studyType;
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;

    public static CafeMyReviewResponse of(Score score, Review review) {
        CafeDetail cafeDetail = review.getCafeDetail();
        return new CafeMyReviewResponse(
                score != null ? score.getScore() : null,
                cafeDetail.getStudyTypeValue(),
                cafeDetail.getWifiValue(),
                cafeDetail.getParkingValue(),
                cafeDetail.getToiletValue(),
                cafeDetail.getPowerValue(),
                cafeDetail.getSoundValue(),
                cafeDetail.getDeskValue()
        );
    }
}
