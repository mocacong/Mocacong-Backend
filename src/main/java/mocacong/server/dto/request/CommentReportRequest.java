package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CommentReportRequest {

    @NotBlank(message = "3009:공백일 수 없습니다.")
    private String myReportReason;
}
