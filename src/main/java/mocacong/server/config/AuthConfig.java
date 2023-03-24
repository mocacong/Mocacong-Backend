package mocacong.server.config;

import mocacong.server.config.logaspect.LogTraceIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthConfig implements WebMvcConfigurer {
    private final LogTraceIdInterceptor logTraceIdInterceptor;

    public AuthConfig(final LogTraceIdInterceptor logTraceIdInterceptor) {
        this.logTraceIdInterceptor = logTraceIdInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logTraceIdInterceptor)
                .addPathPatterns("/**");
    }
}
