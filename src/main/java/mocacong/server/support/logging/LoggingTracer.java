package mocacong.server.support.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingTracer {

    private static final String TRACE_DEPTH_SPACE = "--";

    private final LoggingStatusManager loggingStatusManager;

    public void begin(final String message, final Object[] args) {
        loggingStatusManager.syncStatus();
        if (log.isInfoEnabled()) {
            log.info(
                    "[{}] {}-> {} args={}",
                    loggingStatusManager.getTaskId(),
                    getDepthSpace(loggingStatusManager.getDepthLevel()),
                    message,
                    args
            );
        }
    }

    public void end(final String message) {
        if (log.isInfoEnabled()) {
            long stopTimeMillis = System.currentTimeMillis();
            long resultTimeMillis = stopTimeMillis - loggingStatusManager.getStartTimeMillis();
            log.info(
                    "[{}] <-{} {} time={}ms",
                    loggingStatusManager.getTaskId(),
                    getDepthSpace(loggingStatusManager.getDepthLevel()),
                    message,
                    resultTimeMillis
            );
        }
        loggingStatusManager.release();
    }

    public void exception(String message, Exception e) {
        if (log.isInfoEnabled()) {
            long stopTimeMillis = System.currentTimeMillis();
            long resultTimeMillis = stopTimeMillis - loggingStatusManager.getStartTimeMillis();
            log.info(
                    "[{}] <X{} {} time={}ms ex={}",
                    loggingStatusManager.getTaskId(),
                    getDepthSpace(loggingStatusManager.getDepthLevel()),
                    message,
                    resultTimeMillis,
                    e.toString()
            );
        }
        loggingStatusManager.release();
    }

    private String getDepthSpace(int depthLevel) {
        return TRACE_DEPTH_SPACE.repeat(depthLevel);
    }
}
