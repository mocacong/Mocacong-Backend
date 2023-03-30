package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.domain.Review;
import mocacong.server.domain.cafedetail.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewResponse {

    private Wifi wifi;
    private Parking parking;
    private Toilet toilet;
    private Desk desk;
    private Power power;
    private Sound sound;

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getWifi(),
                review.getParking(),
                review.getToilet(),
                review.getDesk(),
                review.getPower(),
                review.getSound()
        );
    }
}
