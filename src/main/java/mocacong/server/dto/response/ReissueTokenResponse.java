package mocacong.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReissueTokenResponse {
    private String accessToken;

    private int userReportCount;

    public static ReissueTokenResponse from(final String accessToken, int userReportCount) {
        return new ReissueTokenResponse(accessToken, userReportCount);
    }
}
