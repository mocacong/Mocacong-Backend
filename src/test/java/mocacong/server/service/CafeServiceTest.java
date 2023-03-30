package mocacong.server.service;

import java.util.List;
import mocacong.server.domain.Cafe;
import mocacong.server.domain.Member;
import mocacong.server.domain.Score;
import mocacong.server.dto.request.CafeRegisterRequest;
import mocacong.server.dto.response.FindCafeResponse;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.repository.ScoreRepository;
import static org.assertj.core.api.Assertions.assertThat;
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
                () -> assertThat(actual.getScore()).isEqualTo(0.0),
                () -> assertThat(actual.getMyScore()).isNull(),
                () -> assertThat(actual.getStudyType()).isNull(),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(0),
                () -> assertThat(actual.getReviews()).isEmpty(),
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
                () -> assertThat(actual.getScore()).isEqualTo(4.5),
                () -> assertThat(actual.getMyScore()).isEqualTo(score1.getScore()),
                () -> assertThat(actual.getStudyType()).isNull(),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(0),
                () -> assertThat(actual.getReviews()).isEmpty(),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(0),
                () -> assertThat(actual.getComments()).isEmpty()
        );
    }

    // TODO: 리뷰, 코멘트 기능 추가되면 조회 테스트 추가할 것
}
