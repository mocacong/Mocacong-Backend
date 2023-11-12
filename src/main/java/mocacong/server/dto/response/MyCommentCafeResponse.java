package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MyCommentCafeResponse {

    private String mapId;
    private String name;
    private String studyType;
    private String comment;
    private String roadAddress;
}
