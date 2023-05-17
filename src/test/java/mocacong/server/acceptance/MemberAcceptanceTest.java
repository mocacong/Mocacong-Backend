package mocacong.server.acceptance;

import io.restassured.RestAssured;
import static mocacong.server.acceptance.AcceptanceFixtures.*;
import mocacong.server.domain.Platform;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.security.auth.OAuthPlatformMemberResponse;
import mocacong.server.security.auth.apple.AppleOAuthUserProvider;
import mocacong.server.support.AwsSESSender;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class MemberAcceptanceTest extends AcceptanceTest {

    private static final String NONCE = "test";

    @MockBean
    private AppleOAuthUserProvider appleOAuthUserProvider;
    @MockBean
    private AwsSESSender awsSESSender;

    @Test
    @DisplayName("회원을 정상적으로 가입한다")
    void signUp() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("OAuth 회원이 회원가입을 정상적으로 진행한다")
    void signUpOauthMember() {
        String platformId = "1234321";
        String email = "kth@apple.com";
        OAuthPlatformMemberResponse oauthResponse = new OAuthPlatformMemberResponse(platformId, email);
        when(appleOAuthUserProvider.getApplePlatformMember(any())).thenReturn(oauthResponse);
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new AppleLoginRequest("token"))
                .when().post("/login/apple")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        OAuthMemberSignUpRequest request = new OAuthMemberSignUpRequest(null, "케이", Platform.APPLE.getValue(), platformId);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members/oauth")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원이 정상적으로 탈퇴한다")
    void delete() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(request);
        String token = 로그인_토큰_발급(request.getEmail(), request.getPassword());

        RestAssured.given().log().all()
                .auth().oauth2(token)
                .when().delete("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("리뷰나 코멘트를 달은 회원이 정상적으로 탈퇴한다")
    void deleteWhenSaveReviewsAndComments() {
        String mapId = "1234";
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(memberSignUpRequest);
        String token = 로그인_토큰_발급(memberSignUpRequest.getEmail(), memberSignUpRequest.getPassword());

        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));
        카페_리뷰_작성(token, mapId, new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요"));
        카페_코멘트_작성(token, mapId, new CommentSaveRequest("좋아요~"));

        RestAssured.given().log().all()
                .auth().oauth2(token)
                .when().delete("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("즐겨찾기를 등록한 회원이 정상적으로 탈퇴한다")
    void deleteWhenSaveFavorites() {
        String mapId = "1234";
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(memberSignUpRequest);
        String token = 로그인_토큰_발급(memberSignUpRequest.getEmail(), memberSignUpRequest.getPassword());

        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));
        즐겨찾기_등록(token, mapId);

        RestAssured.given().log().all()
                .auth().oauth2(token)
                .when().delete("/members")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("회원은 올바르지 않은 형식의 필드로 가입할 수 없다")
    void signUpInvalidInputField() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "abcdefgh", "케이", "010-1234-5678");
        ErrorResponse response = 회원_가입(request)
                .as(ErrorResponse.class);

        assertThat(response.getCode()).isEqualTo(1007);
    }

    @Test
    @DisplayName("회원가입이 된 이메일로 이메일 인증을 요청할 수 있다")
    void verifyEmail() {
        String email = "kth990303@naver.com";
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(email, "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(memberSignUpRequest);
        doNothing().when(awsSESSender).sendToVerifyEmail(anyString(), anyString());
        EmailVerifyCodeRequest request = new EmailVerifyCodeRequest(NONCE, email);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members/email-verification")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("회원가입이 돼있지 않은 이메일로는 비밀번호 찾기 요청 시에 이메일 인증을 요청할 수 없다")
    void verifyEmailWhenNotRegisteredMember() {
        doNothing().when(awsSESSender).sendToVerifyEmail(anyString(), anyString());
        EmailVerifyCodeRequest request = new EmailVerifyCodeRequest(NONCE, "kth990303@naver.com");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members/email-verification")
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("비밀번호 찾기 요청으로 새로운 비밀번호로 변경한다")
    void findAndResetPassword() {
        String email = "kth990303@naver.com";
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(email, "a1b2c3d4", "케이", "010-1234-5678");
        Long memberId = 회원_가입(memberSignUpRequest).as(MemberSignUpResponse.class).getId();
        ResetPasswordRequest request = new ResetPasswordRequest(NONCE, memberId, "password123");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/members/info/reset-password")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("가입되어 있지 않은 이메일은 이메일 중복검사에서 걸리지 않는다")
    void isDuplicateWithNonExistingEmail() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getEmail())
                .when().get("/members/check-duplicate/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("이미 가입된 이메일은 이메일 중복검사에서 걸린다")
    void isDuplicateWithExistingEmail() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        회원_가입(request);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getEmail())
                .when().get("/members/check-duplicate/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("길이가 0인 이메일은 이메일 중복검사에서 예외를 던진다")
    void emailLengthIs0ReturnException() {
        MemberSignUpRequest request = new MemberSignUpRequest("", "a1b2c3d4", "메리", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getEmail())
                .when().get("/members/check-duplicate/email")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo(1006));
    }

    @Test
    @DisplayName("존재하지 않는 닉네임은 닉네임 중복검사에서 걸리지 않는다")
    void isDuplicateWithNonExistingNickname() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getNickname())
                .when().get("/members/check-duplicate/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("이미 존재하는 닉네임은 닉네임 중복검사에서 걸린다")
    void isDuplicateWithExistingNickname() {
        MemberSignUpRequest request = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getNickname())
                .when().get("/members/check-duplicate/nickname")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("길이가 0인 닉네임은 닉네임 중복검사에서 예외를 던진다")
    void nicknameLengthIs0ReturnException() {
        MemberSignUpRequest request = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "", "010-1234-5678");

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("value", request.getNickname())
                .when().get("/members/check-duplicate/nickname")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo(1009));
    }

    @Test
    @DisplayName("회원을 전체 조회한다")
    void getAllMembers() {
        MemberSignUpRequest request1 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(request1);
        MemberSignUpRequest request2 = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1234-5678");
        회원_가입(request2);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/members/all")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .body();
    }

    @Test
    @DisplayName("마이페이지로 내 정보를 조회한다")
    void findMyInfo() {
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());

        MyPageResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/members/mypage")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MyPageResponse.class);

        assertAll(
                () -> assertThat(actual.getNickname()).isEqualTo("케이"),
                () -> assertThat(actual.getImgUrl()).isNull()
        );
    }

    @Test
    @DisplayName("마이페이지에서 즐겨찾기 등록한 카페 목록을 조회한다")
    void findMyFavoriteCafes() {
        String mapId = "12332312";
        카페_등록(new CafeRegisterRequest(mapId, "메리네 카페"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        즐겨찾기_등록(token, mapId);

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/members/mypage/stars?page=0&count=20")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    @DisplayName("마이페이지에서 리뷰를 등록한 카페 목록을 조회한다")
    void findMyReviewCafes() {
        String mapId1 = "12332312";
        String mapId2 = "12121212";
        카페_등록(new CafeRegisterRequest(mapId1, "메리네 카페"));
        카페_등록(new CafeRegisterRequest(mapId2, "케이네 카페"));
        MemberSignUpRequest signUpRequest1 = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("dlawotn3@naver.com", "a1b2c3d4", "메리", "010-1111-1111");
        회원_가입(signUpRequest1);
        회원_가입(signUpRequest2);
        String token1 = 로그인_토큰_발급(signUpRequest1.getEmail(), signUpRequest1.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest2.getPassword());
        CafeReviewRequest request1 = new CafeReviewRequest(4, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        CafeReviewRequest request2 = new CafeReviewRequest(2, "solo", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        CafeReviewRequest request3 = new CafeReviewRequest(1, "group", "빵빵해요", "여유로워요",
                "깨끗해요", "충분해요", "조용해요", "편해요");
        카페_리뷰_작성(token1, mapId1, request1);
        카페_리뷰_작성(token1, mapId2, request2);
        카페_리뷰_작성(token2, mapId1, request3);

        MyReviewCafesResponse actual = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token1)
                .when().get("/members/mypage/reviews?page=0&count=20")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MyReviewCafesResponse.class);

        assertAll(
                () -> assertThat(actual.getCafes().get(0).getMyScore()).isEqualTo(4),
                () -> assertThat(actual.getCafes().get(0).getName()).isEqualTo("메리네 카페"),
                () -> assertThat(actual.getCafes().get(1).getMyScore()).isEqualTo(2),
                () -> assertThat(actual.getCafes().get(1).getName()).isEqualTo("케이네 카페")
        );
    }

    @Test
    @DisplayName("마이페이지에서 댓글을 작성한 카페 목록을 조회한다")
    void findMyCommentCafes() {
        String mapId1 = "12332312";
        String mapId2 = "12121212";
        카페_등록(new CafeRegisterRequest(mapId1, "메리네 카페"));
        카페_등록(new CafeRegisterRequest(mapId2, "케이네 카페"));
        MemberSignUpRequest signUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(signUpRequest);
        MemberSignUpRequest signUpRequest2 = new MemberSignUpRequest("mery@naver.com", "a1b2c3d4", "메리", "010-1234-5678");
        회원_가입(signUpRequest2);
        String token = 로그인_토큰_발급(signUpRequest.getEmail(), signUpRequest.getPassword());
        String token2 = 로그인_토큰_발급(signUpRequest2.getEmail(), signUpRequest.getPassword());
        카페_코멘트_작성(token, mapId1, new CommentSaveRequest("댓글"));
        카페_코멘트_작성(token, mapId1, new CommentSaveRequest("댓글2"));
        카페_코멘트_작성(token, mapId2, new CommentSaveRequest("댓글3"));
        카페_코멘트_작성(token2, mapId1, new CommentSaveRequest("댓글4"));

        MyCommentCafesResponse response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(token)
                .when().get("/members/mypage/comments?page=0&count=20")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(MyCommentCafesResponse.class);

        assertThat(response.getCafes()).hasSize(3);
    }

    @Test
    @DisplayName("회원이 정상적으로 회원정보를 수정한다")
    void updateProfileInfo() {
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(memberSignUpRequest);
        String token = 로그인_토큰_발급(memberSignUpRequest.getEmail(), memberSignUpRequest.getPassword());
        String newNickname = "메리";
        String newPassword = "jisu1234";
        String newPhone = "010-1234-5678";
        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(newNickname, newPassword, newPhone);

        RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/members/info")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        MyPageResponse actual = 회원정보_조회(token).as(MyPageResponse.class);
        assertAll(
                () -> assertThat(actual.getNickname()).isEqualTo("메리"),
                () -> assertThat(로그인_토큰_발급(memberSignUpRequest.getEmail(), newPassword)).isNotNull()
        );
    }

    @Test
    @DisplayName("회원이 옳은 비밀번호로 비밀번호 인증한다")
    void verifyPasswordWithTrue() {
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(memberSignUpRequest);
        String token = 로그인_토큰_발급(memberSignUpRequest.getEmail(), memberSignUpRequest.getPassword());
        PasswordVerifyRequest request = new PasswordVerifyRequest("a1b2c3d4");

        PasswordVerifyResponse actual = RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members/info/password")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PasswordVerifyResponse.class);

        assertThat(actual.getIsSuccess()).isTrue();
    }

    @Test
    @DisplayName("회원이 틀린 비밀번호로 비밀번호 인증한다")
    void verifyPasswordWithFalse() {
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("kth990303@naver.com", "a1b2c3d4", "케이", "010-1234-5678");
        회원_가입(memberSignUpRequest);
        String token = 로그인_토큰_발급(memberSignUpRequest.getEmail(), memberSignUpRequest.getPassword());
        PasswordVerifyRequest request = new PasswordVerifyRequest("wrongpwd123");

        PasswordVerifyResponse actual = RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/members/info/password")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(PasswordVerifyResponse.class);

        assertThat(actual.getIsSuccess()).isFalse();
    }
}
