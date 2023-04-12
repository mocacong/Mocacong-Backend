package mocacong.server.domain;

import java.util.Arrays;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mocacong.server.exception.badrequest.InvalidPlatformException;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum Platform {

    APPLE("apple"),
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google"),
    MOCACONG("mocacong");

    private String value;

    public static Platform from(String value) {
        return Arrays.stream(values())
                .filter(it -> Objects.equals(it.value, value))
                .findFirst()
                .orElseThrow(InvalidPlatformException::new);
    }
}
