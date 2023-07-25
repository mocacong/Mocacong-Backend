package mocacong.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenResponse {
    private String accessToken;

    private String refreshToken;

    private int userReportCount;

    public static TokenResponse from(final String accessToken, final String refreshToken, int userReportCount) {
        return new TokenResponse(accessToken, refreshToken, userReportCount);
    }
}
