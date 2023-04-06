package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;

    @Test
    @DisplayName("카페 id, 멤버 id로 즐겨찾기 id를 조회한다")
    void findByCafeIdAndMemberId() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페"));
        Member savedMember = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이", "010-1234-5678"));
        Favorite favorite = new Favorite(savedMember, savedCafe);
        favoriteRepository.save(favorite);

        Long actual = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(savedCafe.getId(), savedMember.getId())
                .orElse(null);

        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isEqualTo(favorite.getId())
        );
    }
}
