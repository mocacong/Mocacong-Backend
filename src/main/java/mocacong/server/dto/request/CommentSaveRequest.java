package mocacong.server.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CommentSaveRequest {

    @NotBlank(message = "4002:공백일 수 없습니다.")
    private String content;
}
