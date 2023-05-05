package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class CommentResponse {

    private String imgUrl;
    private String nickname;
    private String content;
    private Boolean isMe;
}
