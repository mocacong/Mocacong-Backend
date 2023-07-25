package mocacong.server.service;

import mocacong.server.domain.*;
import mocacong.server.dto.response.CafeImageReportResponse;
import mocacong.server.dto.response.CommentReportResponse;
import mocacong.server.dto.response.CommentSaveResponse;
import mocacong.server.dto.response.CommentsResponse;
import mocacong.server.exception.badrequest.*;
import mocacong.server.repository.CafeImageRepository;
import mocacong.server.repository.CafeRepository;
import mocacong.server.repository.CommentRepository;
import mocacong.server.repository.MemberRepository;
import mocacong.server.support.AwsS3Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ServiceTest
public class ReportServiceTest {

    @Autowired
    private ReportService reportService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CafeService cafeService;

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CafeRepository cafeRepository;
    @Autowired
    private CafeImageRepository cafeImageRepository;

    @MockBean
    private AwsS3Uploader awsS3Uploader;

    @Test
    @DisplayName("타 사용자가 작성한 댓글을 신고한다")
    void reportComment() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        String reportReason = "insult";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        commentService.save(member1.getId(), mapId, "이 카페 완전 돈 아깝;;");

        CommentReportResponse response = reportService.reportComment(member2.getId(), 1L, reportReason);

        assertThat(response.getCommentReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("본인이 작성한 댓글에 대해 신고를 시도할 시 예외를 반환한다")
    void reportMyComment() {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member.getId(), mapId, "굳이 이런데 가야하나 ㅋ");

        assertThatThrownBy(() -> reportService.reportComment(member.getId(), saveResponse.getId(), "insult"))
                .isInstanceOf(InvalidCommentReportException.class);
    }

    @Test
    @DisplayName("잘못된 신고 사유로 신고를 시도할 시 예외를 반환한다")
    void reportByInvalidReportReason() {
        String mapId = "2143154352323";
        Member member1 = new Member("kth990303@naver.com", "encodePassword", "케이");
        Member member2 = new Member("dlawotn3@naver.com", "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        commentService.save(member1.getId(), mapId, "이 카페 완전 돈 아깝;;");

        assertThatThrownBy(() -> reportService.reportComment(member2.getId(), 1L,
                "invalidReportReason"))
                .isInstanceOf(InvalidReportReasonException.class);
    }

    @Test
    @DisplayName("이미 신고한 댓글에 대해 신고를 시도할 시 예외를 반환한다")
    void reportDuplicateComment() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(member1.getId(), mapId, "아~ 소설보고 싶다");

        reportService.reportComment(member2.getId(), saveResponse.getId(), "inappropriate_content");

        assertThatThrownBy(() -> reportService.reportComment(member2.getId(), saveResponse.getId(),
                "inappropriate_content"))
                .isInstanceOf(DuplicateReportCommentException.class);
    }

    @Test
    @DisplayName("5번 이상 신고된 댓글은 마스킹되며 해당 작성자의 신고 횟수가 1씩 증가한다")
    void maskCauseReport5timesReportedComment() {
        String mapId = "2143154352323";
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Member member = new Member("dlawotn" + i + "@naver.com", "encodePassword", "메리"
                    + (char) ('A' + i));
            members.add(member);
            memberRepository.save(member);
        }
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        CommentSaveResponse saveResponse = commentService.save(members.get(0).getId(), mapId, "아~ 소설보고 싶다");

        for (int i = 1; i <= 4; i++) {
            reportService.reportComment(members.get(i).getId(), saveResponse.getId(),
                    "inappropriate_content");
        }
        CommentReportResponse reportResponse = reportService.reportComment(members.get(5).getId(), saveResponse.getId(),
                "inappropriate_content");
        Optional<Comment> reportedComment = commentRepository.findById(1L);
        Optional<Member> commenter = memberRepository.findById(1L);

        assertAll(
                () -> assertThat(reportResponse.getCommentReportCount()).isEqualTo(5),
                () -> assertThat(reportedComment.get().getContent())
                        .isEqualTo("삭제된 댓글입니다"),
                () -> assertThat(reportedComment.get().getWriterImgUrl()).isNull(),
                () -> assertThat(reportedComment.get().getWriterNickname()).isNull(),
                () -> assertThat(reportedComment.get().isMasked()).isTrue(),
                () -> assertThat(commenter.get().getReportCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("탈퇴한 회원이 작성한 코멘트를 신고한다")
    void reportCommentPostedDeletedMember() {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        Comment comment = new Comment(cafe, member1, "이 카페 완전 돈 아깝;;");
        commentRepository.save(comment);
        memberService.delete(member1.getId());

        CommentReportResponse response = reportService.reportComment(member2.getId(), comment.getId(),
                "inappropriate_content");

        assertThat(response.getCommentReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("타 사용자가 등록한 카페 이미지를 신고한다")
    void reportCafeImage() throws IOException {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        String reportReason = "insult";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg",
                "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));

        CafeImageReportResponse response = reportService.reportCafeImage(member2.getId(), 1L, reportReason);

        assertThat(response.getCafeImageReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("본인이 등록한 카페 이미지에 대해 신고를 시도할 시 예외를 반환한다")
    void reportMyCafeImage() throws IOException {
        String email = "kth990303@naver.com";
        String mapId = "2143154352323";
        Member member = new Member(email, "encodePassword", "케이");
        memberRepository.save(member);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg",
                "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member.getId(), mapId, List.of(mockMultipartFile));

        assertThatThrownBy(() -> reportService.reportCafeImage(member.getId(), 1L, "insult"))
                .isInstanceOf(InvalidCafeImageReportException.class);
    }

    @Test
    @DisplayName("이미 신고한 카페 이미지에 대해 신고를 시도할 시 예외를 반환한다")
    void reportDuplicateCafeImage() throws IOException {
        String email1 = "kth990303@naver.com";
        String email2 = "dlawotn3@naver.com";
        String mapId = "2143154352323";
        Member member1 = new Member(email1, "encodePassword", "케이");
        Member member2 = new Member(email2, "encodePassword", "메리");
        memberRepository.save(member1);
        memberRepository.save(member2);
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg",
                "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(member1.getId(), mapId, List.of(mockMultipartFile));

        reportService.reportCafeImage(member2.getId(), 1L, "inappropriate_content");

        assertThatThrownBy(() -> reportService.reportCafeImage(member2.getId(), 1L, "insult"))
                .isInstanceOf(DuplicateReportCafeImageException.class);
    }

    @Test
    @DisplayName("3번 이상 신고된 카페 이미지는 마스킹되며 해당 작성자의 신고 횟수가 1씩 증가한다")
    void maskCauseReport3timesReportedCafeImage() throws IOException {
        String mapId = "2143154352323";
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            Member member = new Member("dlawotn" + i + "@naver.com", "encodePassword", "메리"
                    + (char) ('A' + i));
            members.add(member);
            memberRepository.save(member);
        }
        Cafe cafe = new Cafe(mapId, "케이카페");
        cafeRepository.save(cafe);
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/images/test_img.jpg");
        MockMultipartFile mockMultipartFile = new MockMultipartFile("test_img", "test_img.jpg",
                "jpg", fileInputStream);
        when(awsS3Uploader.uploadImage(mockMultipartFile)).thenReturn("test_img.jpg");
        cafeService.saveCafeImage(members.get(0).getId(), mapId, List.of(mockMultipartFile));
        reportService.reportCafeImage(members.get(1).getId(), 1L, "inappropriate_content");
        reportService.reportCafeImage(members.get(2).getId(), 1L, "inappropriate_content");

        CafeImageReportResponse reportResponse = reportService.reportCafeImage(members.get(3).getId(),
                1L, "inappropriate_content");
        Optional<CafeImage> reportedCafeImage = cafeImageRepository.findById(1L);
        Optional<Member> author = memberRepository.findById(1L);

        assertAll(
                () -> assertThat(reportResponse.getCafeImageReportCount()).isEqualTo(3),
                () -> assertThat(reportedCafeImage.get().isMasked()).isTrue(),
                () -> assertThat(reportedCafeImage.get().getIsUsed()).isFalse(),
                () -> assertThat(author.get().getReportCount()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("11번 이상 신고된 회원은 Status가 INACTIVE로 전환된다")
    void setInactiveCause11timesReportedComment() {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Member member = new Member("dlawotn" + i + "@naver.com", "encodePassword",
                    "메리" + (char) ('A' + i));
            members.add(member);
            memberRepository.save(member);
        }
        for (int i = 1; i <= 11; i++) {
            String mapId = "abc" + (char) ('A' + i);
            cafeRepository.save(new Cafe(mapId, "메리 카페"));
            CommentSaveResponse saveResponse = commentService.save(members.get(0).getId(), mapId,
                    "아~ 소설보고 싶다");
            for (int j = 1; j <= 5; j ++) {
                reportService.reportComment(members.get(j).getId(), saveResponse.getId(),
                        "inappropriate_content");
            }
        }
        CommentsResponse reportedComment = commentService.findAll(members.get(1).getId(),
                "abc" + (char) ('A' + 1), 0, 3);
        Optional<Member> commenter = memberRepository.findById(1L);

        assertAll(
                () -> assertThat(reportedComment.getComments().get(0).getContent())
                        .isEqualTo("삭제된 댓글입니다"),
                () -> assertThat(commenter.get().getReportCount()).isEqualTo(11),
                () -> assertThat(commenter.get().getStatus()).isEqualTo(Status.INACTIVE)
        );
    }
}
