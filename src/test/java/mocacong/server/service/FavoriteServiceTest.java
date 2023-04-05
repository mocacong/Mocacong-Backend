package mocacong.server.service;

import java.util.List;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Favorite;
import mocacong.server.domain.Member;
import mocacong.server.dto.response.FavoriteSaveResponse;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.FavoriteRepository;
import mocacong.server.repository.MemberRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원이 카페를 즐겨찾기 등록한다")
    void save() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        FavoriteSaveResponse actual = favoriteService.save(member.getEmail(), cafe.getMapId());

        List<Favorite> favorites = favoriteRepository.findAll();
        assertAll(
                () -> assertThat(favorites).hasSize(1),
                () -> assertThat(favorites.get(0).getId()).isEqualTo(actual.getFavoriteId())
        );
    }
}
