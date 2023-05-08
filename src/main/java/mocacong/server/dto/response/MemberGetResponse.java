package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MemberGetResponse {

    private Long id;
    private String email;
    private String nickname;
    private String phone;
}
