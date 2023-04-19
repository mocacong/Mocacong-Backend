package mocacong.server.service;

import java.util.List;
import mocacong.server.domain.*;
import mocacong.server.dto.request.CafeFilterRequest;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.request.CafeReviewRequest;
import mocacong.server.dto.request.CafeReviewUpdateRequest;
import mocacong.server.dto.response.CafeFilterResponse;
import mocacong.server.dto.response.CafeReviewResponse;
import mocacong.server.dto.response.CafeReviewUpdateResponse;
import mocacong.server.dto.response.FindCafeResponse;
import mocacong.server.exception.badrequest.AlreadyExistsCafeReview;
import mocacong.server.exception.notfound.NotFoundReviewException;
import mocacong.server.repository.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@ServiceTest
class CafeServiceTest {

    @Autowired
    private CafeService cafeService;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;

    @Test
    @DisplayName("등록되지 않은 카페를 성공적으로 등록한다")
    void cafeSave() {
        CafeRegisterRequest request = new CafeRegisterRequest("20", "메리네 카페");

        cafeService.save(request);

        List<Cafe> actual = cafeRepository.findAll();
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("등록되어 있는 카페는 등록하지 않는다")
    void cafeSaveDuplicate() {
        CafeRegisterRequest request1 = new CafeRegisterRequest("20", "메리네 카페");
        cafeService.save(request1);

        CafeRegisterRequest request2 = new CafeRegisterRequest("20", "카페");
        cafeService.save(request2);

        List<Cafe> actual = cafeRepository.findAll();
        assertThat(actual).hasSize(1);
    }

    @Test
    @DisplayName("평점, 리뷰 및 코멘트가 존재하지 않는 카페를 조회한다")
    void findCafe() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        FindCafeResponse actual = cafeService.findCafeByMapId(member.getEmail(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isFalse(),
                () -> assertThat(actual.getFavoriteId()).isNull(),
                () -> assertThat(actual.getScore()).isEqualTo(0.0),
                () -> assertThat(actual.getMyScore()).isNull(),
                () -> assertThat(actual.getStudyType()).isNull(),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(0),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(0),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(0),
                () -> assertThat(actual.getComments()).isEmpty()
        );
    }

    @Test
    @DisplayName("평점이 존재하고 리뷰, 코멘트가 존재하지 않는 카페를 조회한다")
    void findCafeWithScore() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Score score1 = new Score(4, member1, cafe);
        scoreRepository.save(score1);
        Score score2 = new Score(5, member2, cafe);
        scoreRepository.save(score2);

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getEmail(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isFalse(),
                () -> assertThat(actual.getFavoriteId()).isNull(),
                () -> assertThat(actual.getScore()).isEqualTo(4.5),
                () -> assertThat(actual.getMyScore()).isEqualTo(score1.getScore()),
                () -> assertThat(actual.getStudyType()).isNull(),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(0),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(0),
                () -> assertThat(actual.getComments()).isEmpty()
        );
    }

    @Test
    @DisplayName("평점, 리뷰, 코멘트가 모두 존재하는 카페를 조회한다")
    void findFavoriteCafeWithAll() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(1, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));
        Comment comment1 = new Comment(cafe, member1, "이 카페 조금 아쉬운 점이 많아요 ㅠㅠ");
        commentRepository.save(comment1);
        Comment comment2 = new Comment(cafe, member2, "와이파이가 왜케 느릴까요...");
        commentRepository.save(comment2);
        Comment comment3 = new Comment(cafe, member1, "다시 와봐도 똑같네요. 리뷰 수정할까 하다가 그대로 남겨요..");
        commentRepository.save(comment3);

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getEmail(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isFalse(),
                () -> assertThat(actual.getFavoriteId()).isNull(),
                () -> assertThat(actual.getScore()).isEqualTo(1.5),
                () -> assertThat(actual.getMyScore()).isEqualTo(1),
                () -> assertThat(actual.getStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(2),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(3),
                () -> assertThat(actual.getComments())
                        .extracting("nickname")
                        .containsExactlyInAnyOrder("케이", "메리", "케이")
        );
    }

    @Test
    @DisplayName("평점, 리뷰, 코멘트가 존재하고 즐겨찾기가 등록된 카페를 조회한다")
    void findCafeWithAll() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(1, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));
        Comment comment1 = new Comment(cafe, member1, "이 카페 조금 아쉬운 점이 많아요 ㅠㅠ");
        commentRepository.save(comment1);
        Comment comment2 = new Comment(cafe, member2, "와이파이가 왜케 느릴까요...");
        commentRepository.save(comment2);
        Comment comment3 = new Comment(cafe, member1, "다시 와봐도 똑같네요. 리뷰 수정할까 하다가 그대로 남겨요..");
        commentRepository.save(comment3);
        Favorite favorite = new Favorite(member1, cafe);
        favoriteRepository.save(favorite);

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getEmail(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isTrue(),
                () -> assertThat(actual.getFavoriteId()).isEqualTo(favorite.getId()),
                () -> assertThat(actual.getScore()).isEqualTo(1.5),
                () -> assertThat(actual.getMyScore()).isEqualTo(1),
                () -> assertThat(actual.getStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(2),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(3),
                () -> assertThat(actual.getComments())
                        .extracting("nickname")
                        .containsExactlyInAnyOrder("케이", "메리", "케이")
        );
    }

    @Test
    @DisplayName("카페에 대한 리뷰를 작성하면 해당 카페 평점과 세부정보가 갱신된다")
    void saveCafeReview() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        CafeReviewResponse actual = cafeService.saveCafeReview(member2.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(2, "solo", "빵빵해요", "협소해요",
                        "깨끗해요", "충분해요", "적당해요", "편해요"));

        assertAll(
                () -> assertThat(actual.getScore()).isEqualTo(3.0),
                () -> assertThat(actual.getStudyType()).isEqualTo("solo"),
                () -> assertThat(actual.getWifi()).isEqualTo("빵빵해요"),
                // `여유로워요`, `협소해요` 둘 중 무엇을 반환해도 괜찮지만 `없어요`는 불가능
                () -> assertThat(actual.getParking()).isNotEqualTo("없어요"),
                () -> assertThat(actual.getToilet()).isEqualTo("깨끗해요"),
                () -> assertThat(actual.getPower()).isEqualTo("충분해요"),
                () -> assertThat(actual.getSound()).isNotEqualTo("북적북적해요"),
                () -> assertThat(actual.getDesk()).isEqualTo("편해요")
        );
    }

    @Test
    @DisplayName("카페 리뷰 작성 후 studyType의 타입 개수가 동일하면 both를 반환한다")
    void saveCafeAndStudyTypesEquals() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        cafeService.saveCafeReview(member1.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        CafeReviewResponse actual = cafeService.saveCafeReview(member2.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "빵빵해요", "협소해요",
                        "깨끗해요", "충분해요", "적당해요", "편해요"));

        assertThat(actual.getStudyType()).isEqualTo("both");
    }

    @Test
    @DisplayName("이미 리뷰를 작성했으면 수정만 가능하고 새로 작성은 불가능하다")
    void cannotSaveManyReviews() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리", "010-1234-5679");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        assertThatThrownBy(() -> cafeService.saveCafeReview(member1.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "빵빵해요", "협소해요",
                        "깨끗해요", "충분해요", "적당해요", "편해요")))
                .isInstanceOf(AlreadyExistsCafeReview.class);
    }

    @Test
    @DisplayName("등록한 카페 리뷰를 성공적으로 수정한다")
    public void updateCafeReview() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member.getEmail(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        CafeReviewUpdateResponse actual = cafeService.updateCafeReview(member.getEmail(), cafe.getMapId(),
                new CafeReviewUpdateRequest(5, "group", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "불편해요"));

        assertAll(
                () -> assertThat(actual.getScore()).isEqualTo(5.0),
                () -> assertThat(actual.getStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getWifi()).isEqualTo("빵빵해요"),
                () -> assertThat(actual.getParking()).isEqualTo("여유로워요"),
                () -> assertThat(actual.getToilet()).isEqualTo("깨끗해요"),
                () -> assertThat(actual.getPower()).isEqualTo("충분해요"),
                () -> assertThat(actual.getSound()).isEqualTo("조용해요"),
                () -> assertThat(actual.getDesk()).isEqualTo("불편해요")
        );
    }

    @Test
    @DisplayName("카페 리뷰를 등록한 적이 없다면 리뷰 수정은 불가능하다")
    public void updateCafeReviewNotFoundReview() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        assertThatThrownBy(() -> cafeService.updateCafeReview(member.getEmail(), cafe.getMapId(), new CafeReviewUpdateRequest(5,
                "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "불편해요")))
                .isInstanceOf(NotFoundReviewException.class);
    }

    @Test
    @DisplayName("studyTypeValue가 주어진 경우 해당 카페 목록을 필터링한다")
    void getCafesFilter() {
        Member member1 = new Member("dlawotn3@naver.com", "encodePassword", "메리", "010-1234-5678");
        Member member2 = new Member("kth990303@naver.com", "encodePassword", "케이", "010-1234-5678");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe1 = new Cafe("2143154352323", "케이카페");
        Cafe cafe2 = new Cafe("2143154311111", "메리카페");
        Cafe cafe3 = new Cafe("2111111125885", "메리카페 2호점");
        Cafe cafe4 = new Cafe("1585656565441", "메리벅스");
        cafeRepository.save(cafe1);
        cafeRepository.save(cafe2);
        cafeRepository.save(cafe3);
        cafeRepository.save(cafe4);
        cafeService.saveCafeReview(member1.getEmail(), cafe1.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member1.getEmail(), cafe2.getMapId(),
                new CafeReviewRequest(2, "group", "빵빵해요", "여유로워요",
                        "깨끗해요", "없어요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member2.getEmail(), cafe3.getMapId(),
                new CafeReviewRequest(5, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member2.getEmail(), cafe4.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        CafeFilterRequest requestBody = new CafeFilterRequest(
                List.of(cafe1.getMapId(), cafe2.getMapId(), cafe3.getMapId(), cafe4.getMapId())
        );

        CafeFilterResponse filteredCafes = cafeService.filterCafesByStudyType("solo", requestBody);

        assertThat(filteredCafes.getMapIds())
                .containsExactlyInAnyOrder(cafe1.getMapId(), cafe3.getMapId(), cafe4.getMapId());
    }

    @Test
    @DisplayName("studyTypeValue에 해당하는 카페가 없는 경우 빈 리스트를 반환한다")
    void getCafesFilterWhenNoMatch() {
        Member member = new Member("dlawotn3@naver.com", "encodePassword", "메리", "010-1234-5678");
        memberRepository.save(member);
        Cafe cafe1 = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe1);
        cafeService.saveCafeReview(member.getEmail(), cafe1.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        CafeFilterRequest requestBody = new CafeFilterRequest(List.of(cafe1.getMapId()));

        CafeFilterResponse filteredCafes = cafeService.filterCafesByStudyType("group", requestBody);

        assertThat(filteredCafes.getMapIds()).isEmpty();
    }
}
