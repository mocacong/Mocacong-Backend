package mocacong.server.support.logging;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final LoggingTracer loggingTracer;
    private final LoggingStatusManager loggingStatusManager;

    @Pointcut("execution(public * mocacong.server..*(..)) "
            + "&& !execution(public * mocacong.server.support.logging..*(..))")
    private void allComponents() {
    }

    @Pointcut("execution(public * mocacong.server.controller..*Controller.*(..))")
    private void allController() {
    }

    @Around("allComponents()")
    public Object doLogTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        final String message = joinPoint.getSignature().toShortString();
        final Object[] args = joinPoint.getArgs();
        try {
            loggingTracer.begin(message, args);
            Object result = joinPoint.proceed();
            loggingTracer.end(message);
            return result;
        } catch (Exception e) {
            loggingTracer.exception(message, e);
            throw e;
        }
    }

    @Around("allController()")
    public Object doLogRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        loggingStatusManager.syncStatus();
        String taskId = loggingStatusManager.getTaskId();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String method = request.getMethod();
        String requestURI = request.getRequestURI();
        Object[] args = joinPoint.getArgs();
        log.info("[{}] {} {} args={}", taskId, method, requestURI, args);

        try {
            return joinPoint.proceed();
        } finally {
            loggingStatusManager.release();
        }
    }

    @AfterReturning(
            value = "execution(public * mocacong.server.controller..*Controller.*(..))",
            returning = "result"
    )
    public void allControllerResponse(JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String controllerMethodName = methodSignature.getMethod()
                .getName();

        log.info("method: {}, result: {}", controllerMethodName, result);
    }
}
