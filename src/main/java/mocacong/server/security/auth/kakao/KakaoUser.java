package mocacong.server.security.auth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class KakaoUser {

    private Long id;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    public static KakaoUser of(Long id, String email) {
        return new KakaoUser(id, new KakaoAccount(email));
    }

    public String getEmail() {
        return kakaoAccount.getEmail();
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    private static class KakaoAccount {

        private String email;
    }
}
