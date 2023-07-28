package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class RefreshTokenRequest {

    @NotBlank(message = "1012:공백일 수 없습니다.")
    private String refreshToken;
}
