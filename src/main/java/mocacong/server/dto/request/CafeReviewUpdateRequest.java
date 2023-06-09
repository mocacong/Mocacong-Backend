package mocacong.server.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeReviewUpdateRequest {

    @NotNull(message = "3009:공백일 수 없습니다.")
    private Integer myScore;

    @NotBlank(message = "3009:공백일 수 없습니다.")
    private String myStudyType;

    private String myWifi;
    private String myParking;
    private String myToilet;
    private String myPower;
    private String mySound;
    private String myDesk;
}
