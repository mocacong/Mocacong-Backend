package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeReviewUpdateRequest {

    @NotBlank(message = "3009:공백일 수 없습니다.")
    private int myScore;
    private String myStudyType;
    private String myWifi;
    private String myParking;
    private String myToilet;
    private String myPower;
    private String mySound;
    private String myDesk;
}
