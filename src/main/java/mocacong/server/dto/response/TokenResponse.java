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

    private int userReportCount;

    public static TokenResponse from(final String token, Status status, int userReportCount) {
        return new TokenResponse(token, status, userReportCount);
    }
}
