package mocacong.server.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class OAuthMemberSignUpRequest {

    private String email;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String nickname;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String platform;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String platformId;
}
