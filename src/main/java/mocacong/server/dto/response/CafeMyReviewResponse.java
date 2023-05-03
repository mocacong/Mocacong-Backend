package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.Review;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CafeMyReviewResponse {

    private String studyType;
    private String wifi;
    private String parking;
    private String toilet;
    private String power;
    private String sound;
    private String desk;

    public static CafeMyReviewResponse from(Review review) {
        return new CafeMyReviewResponse(
                review.getStudyType().getValue(),
                review.getWifi().getValue(),
                review.getParking().getValue(),
                review.getToilet().getValue(),
                review.getPower().getValue(),
                review.getSound().getValue(),
                review.getDesk().getValue()
        );
    }
}
