package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.CafeDetail;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class CafeReviewResponse {

    private double score;
    private String studyType;
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;
    private int reviewsCount;

    public static CafeReviewResponse of(double score, Cafe cafe) {
        CafeDetail cafeDetail = cafe.getCafeDetail();
        int reviewsCount = cafe.getReviews().size();
        return new CafeReviewResponse(
                score,
                cafeDetail.getStudyTypeValue(),
                cafeDetail.getWifiValue(),
                cafeDetail.getParkingValue(),
                cafeDetail.getToiletValue(),
                cafeDetail.getPowerValue(),
                cafeDetail.getSoundValue(),
                cafeDetail.getDeskValue(),
                reviewsCount
        );
    }
}
