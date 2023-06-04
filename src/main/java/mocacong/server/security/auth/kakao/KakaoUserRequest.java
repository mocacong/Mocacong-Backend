package mocacong.server.security.auth.kakao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserRequest {

    private String secure_resource;
    private String property_keys;

    public KakaoUserRequest(String propertyKeys) {
        this.secure_resource = "true";
        this.property_keys = propertyKeys;
    }
}
