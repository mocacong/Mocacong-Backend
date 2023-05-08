package mocacong.server.support.logging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoggingStatus {

    public static final int END_DEPTH_LEVEL = 0;

    private final String taskId;
    private final long startTimeMillis;

    private int depthLevel = 0;

    public void enterDepth() {
        depthLevel++;
    }

    public void comeOutDepth() {
        depthLevel--;
    }

    public boolean isEndDepth() {
        return depthLevel == END_DEPTH_LEVEL;
    }
}
