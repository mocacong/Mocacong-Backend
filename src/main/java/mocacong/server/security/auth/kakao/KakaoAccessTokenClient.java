package mocacong.server.security.auth.kakao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "kakao-access-token-client", url = "https://kauth.kakao.com/oauth/token")
public interface KakaoAccessTokenClient {

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    KakaoAccessToken getToken(@SpringQueryMap KakaoAccessTokenRequest request);
}
