package mocacong.server.support;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DatabaseCleanerCallback implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        SpringExtension.getApplicationContext(context)
                .getBean(DatabaseCleaner.class)
                .execute();
    }
}
