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
    private String token;

    private int userReportCount;

    public static TokenResponse from(final String token, int userReportCount) {
        return new TokenResponse(token, userReportCount);
    }
}
