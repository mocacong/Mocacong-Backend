package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.domain.Score;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
class ScoreRepositoryTest {

    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("카페 id, 멤버 id로 해당 멤버가 특정 카페에 등록한 평점을 조회한다")
    void findScoreByCafeIdAndMemberId() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member member = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이"));
        Score score = new Score(4, member, savedCafe);
        scoreRepository.save(score);

        int actual = scoreRepository.findScoreByCafeIdAndMemberId(savedCafe.getId(), member.getId());

        assertThat(actual).isEqualTo(score.getScore());
    }
}
