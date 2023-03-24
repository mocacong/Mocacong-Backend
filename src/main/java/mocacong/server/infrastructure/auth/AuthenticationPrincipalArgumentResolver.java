package mocacong.server.infrastructure.auth;

import mocacong.server.domain.LoginEmail;
import mocacong.server.dto.LoginUserEmail;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationPrincipalArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtUtils jwtUtils;

    public AuthenticationPrincipalArgumentResolver(final JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginEmail.class);
    }


    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        LoginEmail loginEmailParameterAnnotation = parameter.getParameterAnnotation(LoginEmail.class);
        if (loginEmailParameterAnnotation.isOptional() && !AuthorizationExtractor.hasAccessToken(request)) {
            return LoginUserEmail.NO_LOGIN;
        }

        String token = AuthorizationExtractor.extractAccessToken(request);
        String email = jwtUtils.getPayload(token);
        return LoginUserEmail.from(email);
    }
}
