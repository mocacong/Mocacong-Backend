package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CommentUpdateRequest {

    @NotBlank(message = "4002:공백일 수 없습니다.")
    private String content;
}
