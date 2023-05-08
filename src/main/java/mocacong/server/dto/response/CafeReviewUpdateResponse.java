package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.CafeDetail;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class CafeReviewUpdateResponse {

    private double score;
    private String studyType;
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;

    public static CafeReviewUpdateResponse of(double score, Cafe cafe) {
        CafeDetail cafeDetail = cafe.getCafeDetail();
        return new CafeReviewUpdateResponse(
                score,
                cafeDetail.getStudyTypeValue(),
                cafeDetail.getWifi().getValue(),
                cafeDetail.getParking().getValue(),
                cafeDetail.getToilet().getValue(),
                cafeDetail.getPower().getValue(),
                cafeDetail.getSound().getValue(),
                cafeDetail.getDesk().getValue()
        );
    }
}
