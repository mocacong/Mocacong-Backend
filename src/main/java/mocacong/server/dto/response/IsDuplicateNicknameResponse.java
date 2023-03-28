package mocacong.server.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class IsDuplicateNicknameResponse {

    private boolean result;

    public boolean isDuplicate() {
        return result;
    }
}
