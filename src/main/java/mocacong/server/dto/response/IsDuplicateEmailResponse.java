package mocacong.server.dto.response;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IsDuplicateEmailResponse {

    private boolean result;

    public boolean isDuplicate() {
        return result;
    }
}
