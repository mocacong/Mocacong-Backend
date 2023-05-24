package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyPageResponse {

    private String email;
    private String nickname;
    private String phone;
    private String imgUrl;
}
