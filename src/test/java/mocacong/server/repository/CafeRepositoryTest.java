package mocacong.server.repository;

import mocacong.server.domain.*;
import mocacong.server.domain.cafedetail.*;
import mocacong.server.dto.response.MyReviewCafeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RepositoryTest
class CafeRepositoryTest {

    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ScoreRepository scoreRepository;

    @Test
    @DisplayName("내가 즐겨찾기에 등록한 카페 mapId 목록을 조회한다.")
    void findNearCafeMapIdsByMyFavoriteCafes() {
        String mapId1 = "1";
        String mapId2 = "2";
        String mapId3 = "3";
        String mapId4 = "4";
        Cafe savedCafe1 = cafeRepository.save(new Cafe(mapId1, "케이카페", "1001"));
        Cafe savedCafe2 = cafeRepository.save(new Cafe(mapId2, "케이카페", "1002"));
        Cafe savedCafe3 = cafeRepository.save(new Cafe(mapId3, "케이카페", "1003"));
        Cafe savedCafe4 = cafeRepository.save(new Cafe(mapId4, "케이카페", "1004"));
        Member member = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이"));
        favoriteRepository.save(new Favorite(member, savedCafe1));
        favoriteRepository.save(new Favorite(member, savedCafe2));
        favoriteRepository.save(new Favorite(member, savedCafe3));
        List<String> mapIds = List.of(mapId1, mapId2, mapId3, mapId4);

        List<String> actual = cafeRepository.findNearCafeMapIdsByMyFavoriteCafes(member.getId(), mapIds);

        assertAll(
                () -> assertThat(actual).hasSize(3),
                () -> assertThat(actual.get(0)).isEqualTo(mapId1),
                () -> assertThat(actual.get(1)).isEqualTo(mapId2),
                () -> assertThat(actual.get(2)).isEqualTo(mapId3)
        );
    }

    @Test
    @DisplayName("내가 즐겨찾기에 등록한 카페 목록을 조회한다")
    void findByMyFavoriteCafes() {
        Cafe savedCafe1 = cafeRepository.save(new Cafe("1", "케이카페1", "100"));
        Cafe savedCafe2 = cafeRepository.save(new Cafe("2", "케이카페2", "100"));
        Cafe savedCafe3 = cafeRepository.save(new Cafe("3", "케이카페3", "100"));
        Cafe savedCafe4 = cafeRepository.save(new Cafe("4", "케이카페4", "100"));
        Member member = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이"));
        favoriteRepository.save(new Favorite(member, savedCafe1));
        favoriteRepository.save(new Favorite(member, savedCafe2));
        favoriteRepository.save(new Favorite(member, savedCafe3));

        Slice<Cafe> actual = cafeRepository.findByMyFavoriteCafes(member.getId(), PageRequest.of(1, 2));

        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.getNumber()).isEqualTo(1),
                () -> assertThat(actual.isLast()).isTrue(),
                () -> assertThat(actual)
                        .extracting("name")
                        .containsExactly("케이카페3")
        );
    }

    @Test
    @DisplayName("내가 리뷰를 등록한 카페 목록을 조회한다")
    void findByMyReviewCafesById() {
        Member member = memberRepository.save(new Member("kth@naver.com", "abcd1234", "케이"));

        Cafe savedCafe1 = cafeRepository.save(new Cafe("1", "케이카페1", "100"));
        Cafe savedCafe2 = cafeRepository.save(new Cafe("2", "케이카페2", "100"));
        Cafe savedCafe3 = cafeRepository.save(new Cafe("3", "케이카페3", "100"));

        Score score1 = new Score(1, member, savedCafe1);
        Score score2 = new Score(1, member, savedCafe2);
        Score score3 = new Score(1, member, savedCafe3);

        CafeDetail cafeDetail = new CafeDetail(StudyType.BOTH, Wifi.FAST, Parking.COMFORTABLE, Toilet.CLEAN, Desk.NORMAL,
                Power.NONE, Sound.LOUD);

        reviewRepository.save(new Review(member, savedCafe1, cafeDetail));
        reviewRepository.save(new Review(member, savedCafe2, cafeDetail));
        reviewRepository.save(new Review(member, savedCafe3, cafeDetail));

        scoreRepository.save(score1);
        scoreRepository.save(score2);
        scoreRepository.save(score3);

        Slice<MyReviewCafeResponse> actual = cafeRepository.findMyReviewCafesById(member.getId(), PageRequest.of(1, 2));

        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.getNumber()).isEqualTo(1),
                () -> assertThat(actual.isLast()).isTrue(),
                () -> assertThat(actual)
                        .extracting("myStudyType")
                        .containsExactly("both"),
                () -> assertThat(actual)
                        .extracting("name")
                        .containsExactly("케이카페3"),
                () -> assertThat(actual)
                        .extracting("myScore")
                        .containsExactly(1)
        );
    }
}
