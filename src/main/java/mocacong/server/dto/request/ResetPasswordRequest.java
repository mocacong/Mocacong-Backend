package mocacong.server.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class ResetPasswordRequest {

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String nonce;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String password;
}
