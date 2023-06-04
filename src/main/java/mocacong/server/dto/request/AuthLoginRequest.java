package mocacong.server.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class AuthLoginRequest {

    @Email(message = "1006:이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String email;

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String password;
}
