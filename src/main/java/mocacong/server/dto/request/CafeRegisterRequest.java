package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeRegisterRequest {
    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String mapId;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String name;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String roadAddress;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String phoneNumber;
}
