package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class CommentReportResponse {

    private Long id;

    private String reporterNickname;

    private int reportCount;
}
