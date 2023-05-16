package mocacong.server.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class ResetPasswordRequest {

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String nonce;

    @Min(value = 1, message = "1017:요청 회원 id는 자연수여야 합니다.")
    private Long memberId;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String password;
}
