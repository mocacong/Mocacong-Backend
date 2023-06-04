package mocacong.server.security.auth.apple;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "apple-public-key-client", url = "https://appleid.apple.com/auth")
public interface AppleClient {

    @Cacheable(value = "oauthPublicKeyCache", cacheManager = "oauthPublicKeyCacheManager")
    @GetMapping("/keys")
    ApplePublicKeys getApplePublicKeys();
}
