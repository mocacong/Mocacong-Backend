package mocacong.server.repository;

import mocacong.server.domain.Member;
import mocacong.server.domain.Platform;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원의 OAuth 플랫폼과 플랫폼 id 값으로 회원의 id를 조회한다")
    void findIdByPlatformAndPlatformId() {
        Member member = new Member(
                "kth@apple.com",
                "a1b2c3d4",
                "케이",
                "010-1234-1234",
                null,
                Platform.APPLE,
                "1234321"
        );
        Member savedMember = memberRepository.save(member);

        Long actual = memberRepository.findIdByPlatformAndPlatformId(Platform.APPLE, "1234321")
                .orElseThrow();

        assertThat(actual).isEqualTo(savedMember.getId());
    }
}
