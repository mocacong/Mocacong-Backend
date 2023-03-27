package mocacong.server.service;

import lombok.RequiredArgsConstructor;
import mocacong.server.domain.Member;
import mocacong.server.dto.request.AuthLoginRequest;
import mocacong.server.dto.response.TokenResponse;
import mocacong.server.exception.badrequest.PasswordMismatchException;
import mocacong.server.exception.notfound.NotFoundMemberException;
import mocacong.server.repository.MemberRepository;
import mocacong.server.security.auth.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(final AuthLoginRequest authLoginRequest) {
        Member findMember = memberRepository.findByEmail(authLoginRequest.getEmail())
                .orElseThrow(NotFoundMemberException::new);

        validatePassword(findMember, authLoginRequest.getPassword());

        String token = issueToken(findMember);

        return TokenResponse.from(token);
    }

    private String issueToken(final Member findMember) {
        String email = findMember.getEmail();

        return jwtTokenProvider.createToken(email);
    }

    private void validatePassword(final Member findMember, final String password) {
        if (!passwordEncoder.matches(password, findMember.getPassword())) {
            throw new PasswordMismatchException();
        }
    }
}
