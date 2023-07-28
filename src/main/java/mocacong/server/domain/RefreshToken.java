package mocacong.server.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60*60*24*14) // 리프레시 토큰을 발급한 후 14일 동안 유효
public class RefreshToken implements Serializable {

    @Id
    private String id;

    @Indexed
    private String refreshToken;

    private String accessToken;
}
