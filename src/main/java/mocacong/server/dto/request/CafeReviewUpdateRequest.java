package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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

    @NotBlank(message = "3009:공백일 수 없습니다.")
    private String myPower;

    private String mySound;

    @NotBlank(message = "3009:공백일 수 없습니다.")
    private String myDesk;
}
