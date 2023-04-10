package mocacong.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import mocacong.server.dto.request.MemberSignUpRequest;
import mocacong.server.dto.response.*;
import mocacong.server.security.auth.LoginUserEmail;
import mocacong.server.service.MemberService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Members", description = "회원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @PostMapping
    public ResponseEntity<MemberSignUpResponse> signUp(@RequestBody @Valid MemberSignUpRequest request) {
        MemberSignUpResponse response = memberService.signUp(request);
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

    @Operation(summary = "프로필 이미지 수정")
    @SecurityRequirement(name = "JWT")
    @PutMapping(value = "/mypage/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfileImage(
            @LoginUserEmail String email,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile
    ) {
        memberService.updateProfileImage(email, multipartFile);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원탈퇴")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ResponseEntity<Void> delete(@LoginUserEmail String email) {
        memberService.delete(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원전체탈퇴")
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllMembers() {
        memberService.deleteAll();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원전체조회")
    @GetMapping("/all")
    public MemberGetAllResponse getAllMembers() {
        return memberService.getAllMembers();
    }
}
