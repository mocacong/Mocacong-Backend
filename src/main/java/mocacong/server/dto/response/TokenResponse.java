package mocacong.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import mocacong.server.domain.Status;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenResponse {
    private String token;

    private Status status;

    public static TokenResponse from(final String token, Status status) {
        return new TokenResponse(token, status);
    }
}
