package mocacong.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String token;

    public static TokenResponse from(final String token) {
        return new TokenResponse(token);
    }
}
