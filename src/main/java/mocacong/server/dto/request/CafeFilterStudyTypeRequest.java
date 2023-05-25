package mocacong.server.dto.request;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class CafeFilterStudyTypeRequest {
    private List<String> mapIds;
}
