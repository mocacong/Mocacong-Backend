package mocacong.server.domain;

import mocacong.server.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BaseTimeEntityTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("멤버를 저장하면 생성 시각이 자동으로 저장된다")
    public void memberCreatedAtNow() {
        LocalDateTime now = LocalDateTime.now();

        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678");
        memberRepository.save(member);

        assertThat(member.getCreatedTime()).isNotNull();
        assertThat(member.getCreatedTime()).isAfter(now);
    }
}
