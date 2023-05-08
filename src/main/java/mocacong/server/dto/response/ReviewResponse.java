package mocacong.server.dto.response;

import lombok.*;
import mocacong.server.domain.Review;
import mocacong.server.domain.cafedetail.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
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
