package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeImageReportRequest {

    @NotBlank(message = "3009:공백일 수 없습니다.")
    private String myReportReason;
}
