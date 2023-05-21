package mocacong.server.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class MemberProfileUpdateRequest {

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String nickname;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String phone;
}
