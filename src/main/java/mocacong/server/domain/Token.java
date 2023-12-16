package mocacong.server.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.TimeToLive;

import javax.persistence.Id;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    private Long id;

    private String refreshToken;

    private String accessToken;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private long expiration;

    public void setAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    public static String createRefreshToken() {
        return UUID.randomUUID().toString();
    }
}
