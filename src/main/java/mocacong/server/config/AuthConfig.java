package mocacong.server.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mocacong.server.security.auth.AuthenticationPrincipalArgumentResolver;
import mocacong.server.security.auth.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AuthConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final AuthenticationPrincipalArgumentResolver authenticationPrincipalArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/members/**", "/cafes/**")
                .excludePathPatterns("/members", "/members/oauth", "/members/all",
                        "/members/check-duplicate/**", "/members/email-verification", "/members/info/reset-password")
                .excludePathPatterns("/cafes");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticationPrincipalArgumentResolver);
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    }
}
