package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.security.auth.LoginUserId;
import mocacong.server.service.CafeService;
import mocacong.server.service.MemberService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Tag(name = "Members", description = "회원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final CafeService cafeService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<MemberSignUpResponse> signUp(@RequestBody @Valid MemberSignUpRequest request) {
        MemberSignUpResponse response = memberService.signUp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "OAuth 회원가입")
    @PostMapping("/oauth")
    public ResponseEntity<OAuthMemberSignUpResponse> signUp(@RequestBody @Valid OAuthMemberSignUpRequest request) {
        OAuthMemberSignUpResponse response = memberService.signUpByOAuthMember(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이메일 인증코드 발송")
    @PostMapping("/email-verification")
    public ResponseEntity<EmailVerifyCodeResponse> emailVerify(@RequestBody @Valid EmailVerifyCodeRequest request) {
        EmailVerifyCodeResponse response = memberService.sendEmailVerifyCode(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입 이메일 중복체크")
    @GetMapping("/check-duplicate/email")
    public ResponseEntity<IsDuplicateEmailResponse> checkDuplicateEmail(@RequestParam String value) {
        IsDuplicateEmailResponse response = memberService.isDuplicateEmail(value);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입 닉네임 중복체크")
    @GetMapping("/check-duplicate/nickname")
    public ResponseEntity<IsDuplicateNicknameResponse> checkDuplicateNickname(@RequestParam String value) {
        IsDuplicateNicknameResponse response = memberService.isDuplicateNickname(value);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage")
    public ResponseEntity<MyPageResponse> findMyInfo(@LoginUserId Long memberId) {
        MyPageResponse response = memberService.findMyInfo(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 즐겨찾기 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage/stars")
    public ResponseEntity<MyFavoriteCafesResponse> findMyFavoriteCafes(
            @LoginUserId Long memberId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        MyFavoriteCafesResponse response = cafeService.findMyFavoriteCafes(memberId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 리뷰 남긴 카페 목록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage/reviews")
    public ResponseEntity<MyReviewCafesResponse> findMyReviewCafes(
            @LoginUserId Long memberId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        MyReviewCafesResponse response = cafeService.findMyReviewCafes(memberId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 코멘트 목록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage/comments")
    public ResponseEntity<MyCommentCafesResponse> findMyComments(
            @LoginUserId Long memberId,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        MyCommentCafesResponse response = cafeService.findMyCommentCafes(memberId, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 프로필 이미지 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/mypage/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfileImage(
            @LoginUserId Long memberId,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile
    ) {
        memberService.updateProfileImage(memberId, multipartFile);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "프로필 회원정보 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/info")
    public ResponseEntity<Void> updateProfileInfo(
            @LoginUserId Long memberId,
            @RequestBody @Valid MemberProfileUpdateRequest request
    ) {
        memberService.updateProfileInfo(memberId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "프로필 회원정보 수정 페이지 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping(value = "/info")
    public ResponseEntity<GetUpdateProfileInfoResponse> getUpdateProfileInfo(
            @LoginUserId Long memberId
    ) {
        GetUpdateProfileInfoResponse response = memberService.getUpdateProfileInfo(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 확인 인증")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/info/password")
    public ResponseEntity<PasswordVerifyResponse> passwordVerify(
            @LoginUserId Long memberId,
            @RequestBody @Valid PasswordVerifyRequest request
    ) {
        PasswordVerifyResponse response = memberService.verifyPassword(memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/info/reset-password")
    public ResponseEntity<Void> findAndResetPassword(
            @LoginUserId Long memberId,
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        memberService.resetPassword(memberId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원탈퇴")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ResponseEntity<Void> delete(@LoginUserId Long memberId) {
        memberService.delete(memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원전체조회")
    @GetMapping("/all")
    public MemberGetAllResponse getAllMembers() {
        return memberService.getAllMembers();
    }
}
