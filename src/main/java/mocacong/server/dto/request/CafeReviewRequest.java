package mocacong.server.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class CafeReviewRequest {

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
