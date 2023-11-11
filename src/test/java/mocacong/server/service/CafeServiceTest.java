package mocacong.server.service;

import mocacong.server.domain.*;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.exception.badrequest.AlreadyExistsCafeReview;
import mocacong.server.exception.badrequest.DuplicateCafeException;
import mocacong.server.exception.badrequest.ExceedCageImagesTotalCountsException;
import mocacong.server.exception.notfound.NotFoundCafeImageException;
import mocacong.server.exception.notfound.NotFoundReviewException;
import mocacong.server.repository.*;
import mocacong.server.service.event.DeleteNotUsedImagesEvent;
import mocacong.server.support.AwsS3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ServiceTest
class CafeServiceTest {

    @Autowired
    private CafeService cafeService;
    @Autowired
    private MemberService memberService;
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
    @Autowired
    private CafeImageRepository cafeImageRepository;
    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @MockBean
    private AwsS3Uploader awsS3Uploader;

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

        List<Cafe> actual = cafeRepository.findAll();
        assertAll(
                () -> assertThrows(DuplicateCafeException.class, () -> cafeService.save(request2)),
                () -> assertThat(actual).hasSize(1)
        );
    }

    @Test
    @DisplayName("평점, 리뷰 및 코멘트가 존재하지 않는 카페를 조회한다")
    void findCafe() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        FindCafeResponse actual = cafeService.findCafeByMapId(member.getId(), cafe.getMapId());

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
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Score score1 = new Score(4, member1, cafe);
        scoreRepository.save(score1);
        Score score2 = new Score(5, member2, cafe);
        scoreRepository.save(score2);

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getId(), cafe.getMapId());

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
    void findCafeWithReviewsAndComments() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(1, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "both", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));
        Comment comment1 = new Comment(cafe, member1, "이 카페 조금 아쉬운 점이 많아요 ㅠㅠ");
        commentRepository.save(comment1);
        Comment comment2 = new Comment(cafe, member2, "와이파이가 왜케 느릴까요...");
        commentRepository.save(comment2);
        Comment comment3 = new Comment(cafe, member1, "다시 와봐도 똑같네요. 리뷰 수정할까 하다가 그대로 남겨요..");
        commentRepository.save(comment3);

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getId(), cafe.getMapId());

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
    @DisplayName("코멘트에 좋아요가 있는 카페를 조회한다")
    void findCafeWithCommentLike() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(1, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "both", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));
        Comment comment1 = new Comment(cafe, member1, "이 카페 조금 아쉬운 점이 많아요 ㅠㅠ");
        commentRepository.save(comment1);
        Comment comment2 = new Comment(cafe, member2, "와이파이가 왜케 느릴까요...");
        commentRepository.save(comment2);
        Comment comment3 = new Comment(cafe, member1, "다시 와봐도 똑같네요. 리뷰 수정할까 하다가 그대로 남겨요..");
        commentRepository.save(comment3);
        CommentLike commentLike1 = new CommentLike(member1, comment2);
        commentLikeRepository.save(commentLike1);
        CommentLike commentLike2 = new CommentLike(member2, comment1);
        commentLikeRepository.save(commentLike2);

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getId(), cafe.getMapId());

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
                        .containsExactlyInAnyOrder("케이", "메리", "케이"),
                () -> assertThat(actual.getComments())
                        .extracting("likeCount")
                        .containsExactlyInAnyOrder(1, 1, 0)
        );
    }

    @Test
    @DisplayName("평점, 리뷰가 존재하지 않는 카페 정보를 미리보기한다")
    void previewCafe() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        PreviewCafeResponse actual = cafeService.previewCafeByMapId(member.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isFalse(),
                () -> assertThat(actual.getScore()).isEqualTo(0),
                () -> assertThat(actual.getStudyType()).isNull(),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("평점이 존재하고 리뷰가 존재하지 않는 카페 정보를 미리보기한다")
    void previewCafeWithScore() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        scoreRepository.save(new Score(4, member1, cafe));
        scoreRepository.save(new Score(5, member2, cafe));
        favoriteRepository.save(new Favorite(member1, cafe));

        PreviewCafeResponse actual = cafeService.previewCafeByMapId(member1.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isTrue(),
                () -> assertThat(actual.getScore()).isEqualTo(4.5),
                () -> assertThat(actual.getStudyType()).isNull(),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("평점과 리뷰가 모두 존재하는 카페 정보를 미리보기한다")
    void previewCafeWithScoreAndReview() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(3, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "both", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));
        favoriteRepository.save(new Favorite(member1, cafe));

        PreviewCafeResponse actual = cafeService.previewCafeByMapId(member1.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isTrue(),
                () -> assertThat(actual.getScore()).isEqualTo(2.5),
                () -> assertThat(actual.getStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("카페를 조회할 때 댓글은 3개까지만 보여준다")
    void findCafeAndShowLimitComments() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Comment comment1 = new Comment(cafe, member, "댓글1");
        commentRepository.save(comment1);
        Comment comment2 = new Comment(cafe, member, "댓글2");
        commentRepository.save(comment2);
        Comment comment3 = new Comment(cafe, member, "댓글3");
        commentRepository.save(comment3);
        Comment comment4 = new Comment(cafe, member, "댓글4");
        commentRepository.save(comment4);

        FindCafeResponse actual = cafeService.findCafeByMapId(member.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getCommentsCount()).isEqualTo(4),
                () -> assertThat(actual.getComments()).hasSize(3)
        );
    }

    @Test
    @DisplayName("탈퇴한 회원이 작성한 리뷰와 코멘트가 존재하는 카페를 조회한다")
    void findCafeWithReviewsAndCommentsByDeleteMember() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(), new CafeReviewRequest(1,
                "group", "느려요", "없어요",
                "불편해요", "없어요", "북적북적해요", "불편해요"));
        Comment comment = new Comment(cafe, member1, "이 카페 조금 아쉬운 점이 많아요 ㅠㅠ");
        commentRepository.save(comment);
        memberService.delete(member1.getId());
        memberRepository.delete(member1);

        FindCafeResponse actual = cafeService.findCafeByMapId(member2.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getFavorite()).isFalse(),
                () -> assertThat(actual.getFavoriteId()).isNull(),
                () -> assertThat(actual.getScore()).isEqualTo(1.0),
                () -> assertThat(actual.getMyScore()).isNull(),
                () -> assertThat(actual.getStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(1),
                () -> assertThat(actual.getCommentsCount()).isEqualTo(1),
                () -> assertThat(actual.getComments().get(0).getNickname()).isNull()
        );
    }

    @Test
    @DisplayName("평점, 리뷰, 코멘트가 존재하고 즐겨찾기가 등록된 카페를 조회한다")
    void findCafeWithAll() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(1, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
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

        FindCafeResponse actual = cafeService.findCafeByMapId(member1.getId(), cafe.getMapId());

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
    @DisplayName("카페를 조회할 때 이미지는 5개까지만 보여준다")
    void findCafeAndShowLimitImages() throws IOException {
        String expected = "test_img.jpg";
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        Member member2 = new Member("rlawjddn103@naver.com", "encodePassword", "베어");

        memberRepository.save(member);
        memberRepository.save(member2);

        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg",
                fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));

        FindCafeResponse actual = cafeService.findCafeByMapId(member.getId(), mapId);
        CafeImagesResponse actual2 = cafeService.findCafeImages(member.getId(), mapId, 0, 10);

        assertAll(
                () -> assertThat(actual.getCafeImages()).hasSize(5),
                () -> assertThat(actual2.getCafeImages()).hasSize(6)
        );
    }

    @Test
    @DisplayName("회원이 즐겨찾기한 카페 목록들을 보여준다")
    void findMyFavoriteCafes() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(1, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));
        Favorite favorite = new Favorite(member1, cafe);
        favoriteRepository.save(favorite);

        MyFavoriteCafesResponse actual = cafeService.findMyFavoriteCafes(member1.getId(), 0, 3);

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(actual.getCafes().get(0).getScore()).isEqualTo(1.5)
        );
    }

    @Test
    @DisplayName("회원이 리뷰를 남긴 카페 목록들을 보여준다")
    void findMyReviewCafes() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe1 = new Cafe("2143154352323", "케이카페");
        Cafe cafe2 = new Cafe("2154122541112", "메리카페");
        cafeRepository.save(cafe1);
        cafeRepository.save(cafe2);
        cafeService.saveCafeReview(member1.getId(), cafe1.getMapId(),
                new CafeReviewRequest(1, "solo", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member1.getId(), cafe2.getMapId(),
                new CafeReviewRequest(5, "group", "느려요", "없어요",
                        "불편해요", "없어요", "북적북적해요", "불편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe1.getMapId(),
                new CafeReviewRequest(2, "group", "느려요", "없어요",
                        "깨끗해요", "없어요", null, "보통이에요"));

        MyReviewCafesResponse actual = cafeService.findMyReviewCafes(member1.getId(), 0, 3);

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(actual.getCafes().get(0).getMyScore()).isEqualTo(1),
                () -> assertThat(actual.getCafes().get(0).getMyStudyType()).isEqualTo("solo"),
                () -> assertThat(actual.getCafes().get(0).getName()).isEqualTo("케이카페"),
                () -> assertThat(actual.getCafes().get(1).getMyScore()).isEqualTo(5),
                () -> assertThat(actual.getCafes().get(1).getMyStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getCafes().get(1).getName()).isEqualTo("메리카페"),
                () -> assertThat(actual.getCafes()).hasSize(2)
        );
    }

    @Test
    @DisplayName("회원이 댓글을 작성한 카페 목록들을 보여준다")
    void findMyCommentCafes() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe1 = new Cafe("2143154352323", "케이카페");
        Cafe cafe2 = new Cafe("1212121212121", "메리카페");
        cafeRepository.save(cafe1);
        cafeRepository.save(cafe2);
        commentRepository.save(new Comment(cafe1, member1, "댓글1"));
        commentRepository.save(new Comment(cafe1, member1, "댓글2"));
        commentRepository.save(new Comment(cafe2, member1, "댓글3"));
        commentRepository.save(new Comment(cafe2, member2, "댓글4"));

        MyCommentCafesResponse actual = cafeService.findMyCommentCafes(member1.getId(), 0, 3);

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                // 댓글 수는 3개지만, 카페 종류가 2종류이므로 response size는 2개
                () -> assertThat(actual.getCafes()).hasSize(2),
                () -> assertThat(actual.getCafes().get(0).getComments()).containsExactlyInAnyOrder("댓글1", "댓글2"),
                () -> assertThat(actual.getCafes().get(1).getComments()).containsExactlyInAnyOrder("댓글3")
        );
    }

    @Test
    @DisplayName("카페에 대한 리뷰를 작성하면 해당 카페 평점과 세부정보가 갱신된다")
    void saveCafeReview() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        CafeReviewResponse actual = cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
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
                () -> assertThat(actual.getDesk()).isEqualTo("편해요"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("카페 리뷰 작성 후 studyType의 타입 개수가 동일하면 both를 반환한다")
    void saveCafeAndStudyTypesEquals() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        CafeReviewResponse actual = cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "빵빵해요", "협소해요",
                        "깨끗해요", "충분해요", "적당해요", "편해요"));

        assertThat(actual.getStudyType()).isEqualTo("both");
    }

    @Test
    @DisplayName("특정 카페에 내가 작성한 리뷰를 볼 수 있다")
    void findMyCafeReview() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "적당해요", "협소해요",
                        "불편해요", "없어요", "적당해요", "불편해요"));

        CafeMyReviewResponse actual = cafeService.findMyCafeReview(member1.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getMyScore()).isEqualTo(4),
                () -> assertThat(actual.getMyStudyType()).isEqualTo("solo"),
                () -> assertThat(actual.getMyWifi()).isEqualTo("빵빵해요"),
                () -> assertThat(actual.getMyParking()).isEqualTo("여유로워요"),
                () -> assertThat(actual.getMyToilet()).isEqualTo("깨끗해요"),
                () -> assertThat(actual.getMyPower()).isEqualTo("충분해요"),
                () -> assertThat(actual.getMySound()).isEqualTo("조용해요"),
                () -> assertThat(actual.getMyDesk()).isEqualTo("편해요")
        );
    }

    @Test
    @DisplayName("작성하지 않은 카페에 대한 리뷰를 조회하는 경우 null을 반환한다")
    void findNotRegisteredCafeReview() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        CafeMyReviewResponse actual = cafeService.findMyCafeReview(member.getId(), cafe.getMapId());

        assertAll(
                () -> assertThat(actual.getMyScore()).isNull(),
                () -> assertThat(actual.getMyStudyType()).isNull(),
                () -> assertThat(actual.getMyWifi()).isNull(),
                () -> assertThat(actual.getMyParking()).isNull(),
                () -> assertThat(actual.getMyToilet()).isNull(),
                () -> assertThat(actual.getMyPower()).isNull(),
                () -> assertThat(actual.getMySound()).isNull(),
                () -> assertThat(actual.getMyDesk()).isNull()
        );
    }

    @Test
    @DisplayName("이미 리뷰를 작성했으면 수정만 가능하고 새로 작성은 불가능하다")
    void cannotSaveManyReviews() {
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        Member member2 = new Member("mery@naver.com", "encodePassword", "메리");
        memberRepository.save(member2);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        assertThatThrownBy(() -> cafeService.saveCafeReview(member1.getId(), cafe.getMapId(),
                new CafeReviewRequest(2, "group", "빵빵해요", "협소해요",
                        "깨끗해요", "충분해요", "적당해요", "편해요")))
                .isInstanceOf(AlreadyExistsCafeReview.class);
    }

    @Test
    @DisplayName("등록한 카페 리뷰를 성공적으로 수정한다")
    public void updateCafeReview() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        CafeReviewUpdateResponse actual = cafeService.updateCafeReview(member.getId(), cafe.getMapId(),
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
                () -> assertThat(actual.getDesk()).isEqualTo("불편해요"),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("등록한 카페 리뷰를 수정할 때 세부정보를 모두 null 값으로 성공적으로 수정한다")
    public void updateCafeReviewWhenDetailsNull() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));

        CafeReviewUpdateResponse actual = cafeService.updateCafeReview(member.getId(), cafe.getMapId(),
                new CafeReviewUpdateRequest(5, "group", null, null,
                        null, null, null, null));

        assertAll(
                () -> assertThat(actual.getScore()).isEqualTo(5.0),
                () -> assertThat(actual.getStudyType()).isEqualTo("group"),
                () -> assertThat(actual.getWifi()).isNull(),
                () -> assertThat(actual.getParking()).isNull(),
                () -> assertThat(actual.getToilet()).isNull(),
                () -> assertThat(actual.getPower()).isNull(),
                () -> assertThat(actual.getSound()).isNull(),
                () -> assertThat(actual.getDesk()).isNull(),
                () -> assertThat(actual.getReviewsCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("카페 리뷰를 등록한 적이 없다면 리뷰 수정은 불가능하다")
    public void updateCafeReviewNotFoundReview() {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        assertThatThrownBy(() -> cafeService.updateCafeReview(member.getId(), cafe.getMapId(),
                new CafeReviewUpdateRequest(5, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "불편해요")))
                .isInstanceOf(NotFoundReviewException.class);
    }

    @Test
    @DisplayName("studyTypeValue가 주어진 경우 해당 카페 목록을 필터링한다")
    void getCafesFilterStudyType() {
        Member member1 = new Member("dlawotn3@naver.com", "encodePassword", "메리");
        Member member2 = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe1 = new Cafe("2143154352323", "케이카페");
        Cafe cafe2 = new Cafe("2143154311111", "메리카페");
        Cafe cafe3 = new Cafe("2111111125885", "메리카페 2호점");
        Cafe cafe4 = new Cafe("1585656565441", "메리벅스");
        Cafe cafe5 = new Cafe("1582212121441", "메리설빙");
        cafeRepository.save(cafe1);
        cafeRepository.save(cafe2);
        cafeRepository.save(cafe3);
        cafeRepository.save(cafe4);
        cafeRepository.save(cafe5);
        cafeService.saveCafeReview(member1.getId(), cafe1.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member1.getId(), cafe2.getMapId(),
                new CafeReviewRequest(2, "group", "빵빵해요", "여유로워요",
                        "깨끗해요", "없어요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe3.getMapId(),
                new CafeReviewRequest(5, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe4.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        cafeService.saveCafeReview(member2.getId(), cafe5.getMapId(),
                new CafeReviewRequest(4, "both", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        CafeFilterStudyTypeRequest requestBody = new CafeFilterStudyTypeRequest(
                List.of(cafe1.getMapId(), cafe2.getMapId(), cafe3.getMapId(), cafe4.getMapId(), cafe5.getMapId())
        );

        CafeFilterStudyTypeResponse filteredCafes = cafeService.filterCafesByStudyType("solo", requestBody);

        assertThat(filteredCafes.getMapIds())
                .containsExactlyInAnyOrder(cafe1.getMapId(), cafe3.getMapId(), cafe4.getMapId(), cafe5.getMapId());
    }

    @Test
    @DisplayName("studyTypeValue에 해당하는 카페가 없는 경우 빈 리스트를 반환한다")
    void getCafesFilterStudyTypeWhenNoMatch() {
        Member member = new Member("dlawotn3@naver.com", "encodePassword", "메리");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        cafeService.saveCafeReview(member.getId(), cafe.getMapId(),
                new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                        "깨끗해요", "충분해요", "조용해요", "편해요"));
        CafeFilterStudyTypeRequest requestBody = new CafeFilterStudyTypeRequest(List.of(cafe.getMapId()));

        CafeFilterStudyTypeResponse filteredCafes = cafeService.filterCafesByStudyType("group", requestBody);

        assertThat(filteredCafes.getMapIds()).isEmpty();
    }

    @Test
    @DisplayName("즐겨찾기가 등록된 카페가 있는 경우 해당 카페 목록을 필터링한다")
    void getCafesFilterFavorites() {
        Member member = new Member("dlawotn3@naver.com", "encodePassword", "메리");
        memberRepository.save(member);
        Cafe cafe1 = new Cafe("2143154352323", "케이카페");
        Cafe cafe2 = new Cafe("2143154311111", "메리카페");
        Cafe cafe3 = new Cafe("2111111125885", "메리카페 2호점");
        Cafe cafe4 = new Cafe("1585656565441", "메리벅스");
        cafeRepository.save(cafe1);
        cafeRepository.save(cafe2);
        cafeRepository.save(cafe3);
        cafeRepository.save(cafe4);
        favoriteRepository.save(new Favorite(member, cafe1));
        favoriteRepository.save(new Favorite(member, cafe2));
        CafeFilterFavoritesRequest requestBody = new CafeFilterFavoritesRequest(
                List.of(cafe1.getMapId(), cafe2.getMapId(), cafe3.getMapId(), cafe4.getMapId())
        );

        CafeFilterFavoritesResponse filteredCafes = cafeService.filterCafesByFavorites(member.getId(), requestBody);

        assertThat(filteredCafes.getMapIds())
                .containsExactlyInAnyOrder(cafe1.getMapId(), cafe2.getMapId());
    }

    @Test
    @DisplayName("즐겨찾기가 등록된 카페가 없는 경우 빈 리스트를 반환한다")
    void getCafesFilterFavoritesWhenNoMatch() {
        Member member = new Member("dlawotn3@naver.com", "encodePassword", "메리");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);

        CafeFilterFavoritesRequest requestBody = new CafeFilterFavoritesRequest(List.of(cafe.getMapId()));

        CafeFilterFavoritesResponse filteredCafes = cafeService.filterCafesByFavorites(member.getId(), requestBody);

        assertThat(filteredCafes.getMapIds()).isEmpty();
    }

    @Test
    @DisplayName("카페 이미지를 성공적으로 저장한다")
    void saveCafeImage() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg",
                fileInputStream);

        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));

        List<CafeImage> actual = cafeImageRepository
                .findAllByCafeIdAndIsUsedOrderByCafeImageId(cafe.getId(), member.getId(), PageRequest.of(0, 5))
                .getContent();
        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.get(0).getImgUrl()).isEqualTo(expected)
        );
    }

    @Test
    @DisplayName("카페 이미지를 저장한 후 Response를 반환한다.")
    void saveCafeImageWithResponse() throws IOException {
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + "test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg", "jpg",
                fileInputStream);

        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        CafeImagesSaveResponse cafeImagesSaveResponse = cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));

        assertAll(
                () -> assertThat(cafeImagesSaveResponse.getCafeImagesIds()).hasSize(1),
                () -> assertThat(cafeImagesSaveResponse.getCafeImagesIds().get(0).getId()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("카페 이미지를 한 번에 3개까지 저장할 수 있다")
    void saveCafeImagesPerRequest() throws IOException {
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("kth990303@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        MockMultipartFile mockMultipartFile1 =
                new MockMultipartFile("test_img", "test_img.jpg", "jpg", new FileInputStream("src/test/resources/images/test_img.jpg"));
        MockMultipartFile mockMultipartFile2 =
                new MockMultipartFile("test_img2", "test_img2.jpg", "jpg", new FileInputStream("src/test/resources/images/test_img2.jpg"));
        MockMultipartFile mockMultipartFile3 =
                new MockMultipartFile("test_img2", "test_img2.jpg", "jpg", new FileInputStream("src/test/resources/images/test_img2.jpg"));
        when(awsS3Uploader.uploadImage(mockMultipartFile1)).thenReturn("test_img.jpg");
        when(awsS3Uploader.uploadImage(mockMultipartFile2)).thenReturn("test_img2.jpg");
        when(awsS3Uploader.uploadImage(mockMultipartFile3)).thenReturn("test_img2.jpg");

        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile1, mockMultipartFile2, mockMultipartFile3));

        List<CafeImage> actual = cafeImageRepository
                .findAllByCafeIdAndIsUsedOrderByCafeImageId(cafe.getId(), member.getId(), PageRequest.of(0, 5))
                .getContent();
        assertThat(actual).hasSize(3);
    }

    @Test
    @DisplayName("카페 이미지를 한 번에 세 개를 초과하여 저장하면 예외를 반환한다")
    void saveCafeImagesManyPerRequest() throws IOException {
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("kth990303@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        MockMultipartFile mockMultipartFile1 =
                new MockMultipartFile("test_img", "test_img.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img.jpg"));
        MockMultipartFile mockMultipartFile2 =
                new MockMultipartFile("test_img2", "test_img2.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img2.jpg"));
        MockMultipartFile mockMultipartFile3 =
                new MockMultipartFile("test_img", "test_img.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img.jpg"));
        MockMultipartFile mockMultipartFile4 =
                new MockMultipartFile("test_img2", "test_img2.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img2.jpg"));

        assertThatThrownBy(() -> cafeService.saveCafeImage(
                member.getId(),
                mapId,
                List.of(mockMultipartFile1, mockMultipartFile2, mockMultipartFile3, mockMultipartFile4)
        )).isInstanceOf(ExceedCageImagesTotalCountsException.class);
    }

    @Test
    @DisplayName("사용자가 카페 이미지를 여러 번 저장한다")
    void saveCafeImages() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg", fileInputStream);

        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));

        List<CafeImage> actual = cafeImageRepository
                .findAllByCafeIdAndIsUsedOrderByCafeImageId(cafe.getId(), member.getId(), PageRequest.of(0, 5))
                .getContent();

        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.get(0).getImgUrl()).isEqualTo(expected),
                () -> assertThat(actual.get(1).getImgUrl()).isEqualTo(expected)
        );
    }


    @Test
    @DisplayName("사용자가 카페 이미지를 총 3개 보다 많이 저장하면 예외가 발생한다.")
    void saveCafeImagesOver3() throws IOException {
        Cafe cafe = new Cafe("2143154352323", "베어카페");
        cafeRepository.save(cafe);
        Member member = new Member("rlawjddn103@naver.com", "a1b2c3d4", "베어", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        MockMultipartFile mockMultipartFile1 =
                new MockMultipartFile("test_img", "test_img.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img.jpg"));
        MockMultipartFile mockMultipartFile2 =
                new MockMultipartFile("test_img2", "test_img2.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img2.jpg"));
        MockMultipartFile mockMultipartFile3 =
                new MockMultipartFile("test_img", "test_img.jpg", "jpg",
                        new FileInputStream("src/test/resources/images/test_img.jpg"));

        when(awsS3Uploader.uploadImage(mockMultipartFile1)).thenReturn("test_img.jpg");
        when(awsS3Uploader.uploadImage(mockMultipartFile2)).thenReturn("test_img2.jpg");
        when(awsS3Uploader.uploadImage(mockMultipartFile3)).thenReturn("test_img.jpg");

        cafeService.saveCafeImage(
                member.getId(),
                mapId,
                List.of(mockMultipartFile1, mockMultipartFile2, mockMultipartFile3)
        );

        assertThatThrownBy(() -> cafeService.saveCafeImage(
                member.getId(),
                mapId,
                List.of(mockMultipartFile1)
        )).isInstanceOf(ExceedCageImagesTotalCountsException.class);
    }


    @Test
    @DisplayName("사용자가 카페 이미지를 조회한다")
    void findCafeImages() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));

        CafeImagesResponse actual = cafeService.findCafeImages(member.getId(), mapId, 0, 3);

        List<CafeImageResponse> cafeImages = actual.getCafeImages();

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(cafeImages).hasSize(3)
        );
    }

    @Test
    @DisplayName("자신이 등록한 카페 이미지를 조회하는 경우 isMe가 True로 반환된다")
    void findCafeMyImagesReturnTrue() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));
        CafeImagesResponse actual = cafeService.findCafeImages(member.getId(), mapId, 0, 10);

        List<CafeImageResponse> cafeImages = actual.getCafeImages();

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(cafeImages).hasSize(2),
                () -> assertThat(cafeImages.get(0).getIsMe()).isTrue(),
                () -> assertThat(cafeImages.get(1).getIsMe()).isTrue()
        );
    }

    @Test
    @DisplayName("타인이 등록한 카페이미지를 조회할 때 isMe가 false로 반환된다")
    void findOtherCafeImagesReturnFalse() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member1 = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "a1b2c3d4", "다른사람", null);
        memberRepository.save(member2);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg",
                fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        CafeImagesResponse actual = cafeService.findCafeImages(member2.getId(), mapId, 0, 10);

        List<CafeImageResponse> cafeImages = actual.getCafeImages();

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(cafeImages).hasSize(3),
                () -> assertThat(cafeImages.get(0).getIsMe()).isFalse(),
                () -> assertThat(cafeImages.get(1).getIsMe()).isFalse()
        );
    }

    @Test
    @DisplayName("자신이 등록한 이미지부터 등록 순으로 이미지를 조회한다")
    void findCafeImagesReturnOrderedImages() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member1 = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "a1b2c3d4", "다른사람", null);
        memberRepository.save(member2);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg",
                fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));
        CafeImagesResponse actual = cafeService.findCafeImages(member2.getId(), mapId, 0, 10);

        List<CafeImageResponse> cafeImages = actual.getCafeImages();

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(cafeImages).hasSize(4),
                // 등록 순으로 정렬되었는지 검증
                () -> assertThat(cafeImages.get(0).getId()).isEqualTo(3),
                () -> assertThat(cafeImages.get(1).getId()).isEqualTo(4),
                () -> assertThat(cafeImages.get(2).getId()).isEqualTo(1),
                () -> assertThat(cafeImages.get(3).getId()).isEqualTo(2),
                () -> assertThat(cafeImages.get(0).getIsMe()).isTrue(),
                () -> assertThat(cafeImages.get(2).getIsMe()).isFalse()
        );
    }

    @Test
    @DisplayName("타인이 나중에 이미지를 등록해도 자신이 등록한 이미지부터 등록 순으로 조회한다")
    void findCafeImagesReturnOrderedMyImages() throws IOException {
        String expected = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member1 = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member1);
        Member member2 = new Member("kth990303@naver.com", "a1b2c3d4", "다른사람", null);
        memberRepository.save(member2);
        String mapId = cafe.getMapId();
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/" + expected);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", expected, "jpg",
                fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(mockMultipartFile));
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));
        CafeImagesResponse actual = cafeService.findCafeImages(member2.getId(), mapId, 0, 10);

        List<CafeImageResponse> cafeImages = actual.getCafeImages();

        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                () -> assertThat(cafeImages).hasSize(4),
                // 자신이 올린 사진부터 등록 순으로 정렬되었는지 검증
                () -> assertThat(cafeImages.get(0).getId()).isEqualTo(1),
                () -> assertThat(cafeImages.get(1).getId()).isEqualTo(3),
                () -> assertThat(cafeImages.get(2).getId()).isEqualTo(2),
                () -> assertThat(cafeImages.get(3).getId()).isEqualTo(4),
                () -> assertThat(cafeImages.get(0).getIsMe()).isTrue(),
                () -> assertThat(cafeImages.get(2).getIsMe()).isFalse()
        );
    }

    @Test
    @DisplayName("카페 이미지를 성공적으로 수정한다")
    void updateCafeImage() throws IOException {
        String oldImage = "test_img.jpg";
        String newImage = "test_img2.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream oldFileInputStream = new FileInputStream("src/test/resources/images/" + oldImage);
        FileInputStream newFileInputStream = new FileInputStream("src/test/resources/images/" + newImage);
        MockMultipartFile oldMockMultipartFile =
                new MockMultipartFile("test_img", oldImage, "jpg", oldFileInputStream);
        MockMultipartFile newMockMultipartFile =
                new MockMultipartFile("test_img2", newImage, "jpg", newFileInputStream);
        when(awsS3Uploader.uploadImage(oldMockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(oldMockMultipartFile));
        CafeImagesResponse oldFindImage = cafeService.findCafeImages(member.getId(), mapId, 0, 10);
        when(awsS3Uploader.uploadImage(newMockMultipartFile)).thenReturn("test_img2.jpg");

        cafeService.updateCafeImage(member.getId(), mapId, oldFindImage.getCafeImages().get(0).getId(),
                newMockMultipartFile);

        CafeImagesResponse actual = cafeService.findCafeImages(member.getId(), mapId, 0, 10);
        List<CafeImageResponse> cafeImages = actual.getCafeImages();
        assertAll(
                () -> assertThat(actual.getIsEnd()).isTrue(),
                // 수정 이미지의 url 자체만 변경되고 id는 동일할 것
                () -> assertThat(actual.getCafeImages().get(0).getId())
                        .isEqualTo(oldFindImage.getCafeImages().get(0).getId()),
                () -> assertThat(cafeImages.get(0).getImageUrl()).endsWith("test_img2.jpg"),
                () -> assertThat(cafeImages).hasSize(1)
        );
    }

    @Test
    @DisplayName("존재하지 않는 카페 이미지를 수정 시도할 시 예외를 반환한다")
    void updateCafeImageNotFoundImage() throws IOException {
        String newImage = "test_img.jpg";
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        Member member = new Member("dlawotn3@naver.com", "a1b2c3d4", "메리", null);
        memberRepository.save(member);
        String mapId = cafe.getMapId();
        FileInputStream newFileInputStream = new FileInputStream("src/test/resources/images/" + newImage);
        MockMultipartFile newMockMultipartFile = new MockMultipartFile("test_img2", newImage, "jpg",
                newFileInputStream);
        when(awsS3Uploader.uploadImage(newMockMultipartFile)).thenReturn("test_img2.jpg");

        assertThatThrownBy(() -> cafeService.updateCafeImage(member.getId(), mapId, 9999L,
                newMockMultipartFile)).isInstanceOf(NotFoundCafeImageException.class);
    }

    @Test
    @DisplayName("카페 이미지를 수정한 후 조회할 때 isTrue인 이미지를 우선하여 먼저 등록된 이미지들부터 5개까지 보여준다.")
    void updateCafeImageAndShowLimitImages() throws IOException {
        String oldImage = "test_img.jpg";
        String newImage = "test_img2.jpg";
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        Member member2 = new Member("rlawjddn103@naver.com", "encodePassword", "베어");

        memberRepository.save(member);
        memberRepository.save(member2);

        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        String mapId = cafe.getMapId();
        FileInputStream oldFileInputStream = new FileInputStream("src/test/resources/images/" + oldImage);
        FileInputStream newFileInputStream = new FileInputStream("src/test/resources/images/" + newImage);
        MockMultipartFile oldMockMultipartFile =
                new MockMultipartFile("test_img", oldImage, "jpg", oldFileInputStream);
        MockMultipartFile newMockMultipartFile =
                new MockMultipartFile("test_img2", newImage, "jpg", newFileInputStream);
        when(awsS3Uploader.uploadImage(oldMockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(oldMockMultipartFile));
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(oldMockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(oldMockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(oldMockMultipartFile));
        cafeService.saveCafeImage(member.getId(), mapId, List.of(oldMockMultipartFile));

        CafeImagesResponse oldFindImage = cafeService.findCafeImages(member.getId(), mapId, 0, 10);
        when(awsS3Uploader.uploadImage(newMockMultipartFile)).thenReturn("test_img2.jpg");

        cafeService.updateCafeImage(
                member.getId(), mapId, oldFindImage.getCafeImages().get(0).getId(), newMockMultipartFile
        );
        cafeService.saveCafeImage(member2.getId(), mapId, List.of(oldMockMultipartFile));
        FindCafeResponse actual = cafeService.findCafeByMapId(member.getId(), mapId);
        CafeImagesResponse given = cafeService.findCafeImages(member.getId(), mapId, 0, 10);

        assertAll(
                () -> assertThat(actual.getCafeImages()).hasSize(5),
                () -> assertThat(actual.getCafeImages().get(0).getIsMe()).isEqualTo(true),
                () -> assertThat(actual.getCafeImages().get(1).getIsMe()).isEqualTo(true),
                () -> assertThat(actual.getCafeImages().get(2).getIsMe()).isEqualTo(true),
                () -> assertThat(actual.getCafeImages().get(3).getIsMe()).isEqualTo(false),
                () -> assertThat(actual.getCafeImages().get(4).getIsMe()).isEqualTo(false),
                () -> assertThat(actual.getCafeImages().get(0).getImageUrl()).endsWith("test_img2.jpg"),
                () -> assertThat(given.getCafeImages()).hasSize(6)
        );
    }

    @Test
    @DisplayName("탈퇴한 회원의 카페 이미지는 삭제되지 않고 이미지 작성자를 null 처리한다")
    void cafeImagesWhenMemberDelete() throws IOException {
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg",
                "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), cafe.getMapId(), List.of(mockMultipartFile));

        memberService.delete(member.getId());
        List<CafeImage> actual = cafeImageRepository.findAll();

        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.get(0).getMember()).isNull()
        );
    }

    @Test
    @DisplayName("사용하지 않는 카페 이미지들을 삭제한다")
    void deleteNotUsedCafeImages() {
        List<String> notUsedImgUrls = List.of("test_img2.jpg", "test_img3.jpg");
        Member member = new Member("kth990303@naver.com", "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe("2143154352323", "케이카페");
        cafeRepository.save(cafe);
        CafeImage cafeImage1 = new CafeImage("test_img.jpg", true, cafe, member);
        cafeImageRepository.save(cafeImage1);
        CafeImage cafeImage2 = new CafeImage("test_img2.jpg", false, cafe, member);
        cafeImageRepository.save(cafeImage2);
        CafeImage cafeImage3 = new CafeImage("test_img3.jpg", false, cafe, member);
        cafeImageRepository.save(cafeImage3);

        doNothing().when(awsS3Uploader).deleteImages(new DeleteNotUsedImagesEvent(notUsedImgUrls));
        cafeService.deleteNotUsedCafeImages();

        List<CafeImage> actual = cafeImageRepository.findAll();
        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual).extracting("imgUrl")
                        .containsExactlyInAnyOrder("test_img.jpg")
        );
    }
}
