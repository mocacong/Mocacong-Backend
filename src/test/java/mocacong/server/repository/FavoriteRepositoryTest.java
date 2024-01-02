package mocacong.server.repository;

import java.util.List;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RepositoryTest
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
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페", "서울시 강남구"));
        Member savedMember = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이"));
        Favorite favorite = new Favorite(savedMember, savedCafe);
        favoriteRepository.save(favorite);

        Long actual = favoriteRepository.findFavoriteIdByCafeIdAndMemberId(savedCafe.getId(), savedMember.getId())
                .orElse(null);

        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual).isEqualTo(favorite.getId())
        );
    }

    @Test
    @DisplayName("멤버 id가 null인 즐겨찾기들을 모두 삭제한다")
    void deleteAllByMemberIdIsNull() {
        Cafe savedCafe = cafeRepository.save(new Cafe("1", "케이카페", "서울시 강남구"));
        Favorite favorite = new Favorite(null, savedCafe);
        favoriteRepository.save(favorite);

        favoriteRepository.deleteAllByMemberIdIsNull();

        List<Favorite> actual = favoriteRepository.findAll();
        assertThat(actual).isEmpty();
    }
}
