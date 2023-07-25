package mocacong.server.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Id;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60*60*24*3)
public class RefreshToken implements Serializable {

    @Id
    private String id;

    private String refreshToken;

    @Indexed
    private String accessToken;
}