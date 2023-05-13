package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class EmailVerifyCodeResponse {

    private Long id;
    private String code;
}
