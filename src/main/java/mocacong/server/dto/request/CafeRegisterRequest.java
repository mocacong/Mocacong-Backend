package mocacong.server.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeRegisterRequest {
    @NotBlank
    private String id;

    @NotBlank
    private String name;
}
