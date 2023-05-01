package mocacong.server.repository;

import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

@RepositoryTest
class CafeRepositoryTest {

    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    @DisplayName("내가 즐겨찾기에 등록한 카페 목록을 조회한다")
    void findByMyFavoriteCafes() {
        Cafe savedCafe1 = cafeRepository.save(new Cafe("1", "케이카페1"));
        Cafe savedCafe2 = cafeRepository.save(new Cafe("2", "케이카페2"));
        Cafe savedCafe3 = cafeRepository.save(new Cafe("3", "케이카페3"));
        Cafe savedCafe4 = cafeRepository.save(new Cafe("4", "케이카페4"));
        Member member = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이", "010-1234-5678"));
        favoriteRepository.save(new Favorite(member, savedCafe1));
        favoriteRepository.save(new Favorite(member, savedCafe2));
        favoriteRepository.save(new Favorite(member, savedCafe3));

        Slice<Cafe> actual = cafeRepository.findByMyFavoriteCafes(member.getEmail(), PageRequest.of(1, 2));

        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.getNumber()).isEqualTo(1),
                () -> assertThat(actual.isLast()).isTrue(),
                () -> assertThat(actual)
                        .extracting("name")
                        .containsExactly("케이카페3")
        );
    }
}
