package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.CafeDetail;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeReviewResponse {

    private double score;
    private String studyType;
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;

    public static CafeReviewResponse of(double score, Cafe cafe) {
        CafeDetail cafeDetail = cafe.getCafeDetail();
        return new CafeReviewResponse(
                score,
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
