package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.Review;
import mocacong.server.domain.Score;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeMyReviewResponse {

    private Integer myScore;
    private String myStudyType;
    private String myWifi;
    private String myParking;
    private String myToilet;
    private String myPower;
    private String mySound;
    private String myDesk;

    public static CafeMyReviewResponse of(Score score, Review review) {
        return new CafeMyReviewResponse(
                score != null ? score.getScore() : null,
                review != null ? review.getCafeDetail().getStudyTypeValue() : null,
                review != null ? review.getCafeDetail().getWifiValue() : null,
                review != null ? review.getCafeDetail().getParkingValue() : null,
                review != null ? review.getCafeDetail().getToiletValue() : null,
                review != null ? review.getCafeDetail().getPowerValue() : null,
                review != null ? review.getCafeDetail().getSoundValue() : null,
                review != null ? review.getCafeDetail().getDeskValue() : null
        );
    }
}
