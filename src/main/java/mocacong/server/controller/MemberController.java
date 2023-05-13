package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.*;
import mocacong.server.dto.response.*;
import mocacong.server.security.auth.LoginUserEmail;
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
        EmailVerifyCodeResponse response = memberService.sendEmailVerifyCode(request.getEmail());
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
    public ResponseEntity<MyPageResponse> findMyInfo(@LoginUserEmail String email) {
        MyPageResponse response = memberService.findMyInfo(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 즐겨찾기 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage/stars")
    public ResponseEntity<MyFavoriteCafesResponse> findMyInfo(
            @LoginUserEmail String email,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        MyFavoriteCafesResponse response = cafeService.findMyFavoriteCafes(email, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 리뷰 남긴 카페 목록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage/reviews")
    public ResponseEntity<MyReviewCafesResponse> findMyReviewCafes(
            @LoginUserEmail String email,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        MyReviewCafesResponse response = cafeService.findMyReviewCafes(email, page, count);
        return ResponseEntity.ok(response);
    }
    
  @Operation(summary = "마이페이지 - 코멘트 목록 조회")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/mypage/comments")
    public ResponseEntity<MyCommentCafesResponse> findMyComments(
            @LoginUserEmail String email,
            @RequestParam("page") final Integer page,
            @RequestParam("count") final int count
    ) {
        MyCommentCafesResponse response = cafeService.findMyCommentCafes(email, page, count);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 - 프로필 이미지 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/mypage/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfileImage(
            @LoginUserEmail String email,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile
    ) {
        memberService.updateProfileImage(email, multipartFile);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "프로필 회원정보 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/info")
    public ResponseEntity<Void> updateProfileInfo(
            @LoginUserEmail String email,
            @RequestBody @Valid MemberProfileUpdateRequest request
    ) {
        memberService.updateProfileInfo(email, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 확인 인증")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/info/password")
    public ResponseEntity<PasswordVerifyResponse> passwordVerify(
            @LoginUserEmail String email,
            @RequestBody @Valid PasswordVerifyRequest request
    ) {
        PasswordVerifyResponse response = memberService.verifyPassword(email, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원탈퇴")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ResponseEntity<Void> delete(@LoginUserEmail String email) {
        memberService.delete(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원전체조회")
    @GetMapping("/all")
    public MemberGetAllResponse getAllMembers() {
        return memberService.getAllMembers();
    }
}
