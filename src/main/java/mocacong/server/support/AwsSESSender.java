package mocacong.server.support;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AwsSESSender {

    private static final String FROM = "verify-email@mocacong.com";
    private static final String TITLE = "모카콩 이메일 인증";
    private static final String VERIFY_EMAIL_FILE_PATH = "src/main/resources/static/verify-email.html";
    private static final String INITIAL_CODE = "9999";

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    public AwsSESSender(@Value("${aws.ses.access-key}") String accessKey,
                        @Value("${aws.ses.secret-key}") String secretKey) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        this.amazonSimpleEmailService = AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("ap-northeast-2")
                .build();
    }

    @Async
    public void sendToVerifyEmail(String to, String code) {
        try {
            File file = new File(VERIFY_EMAIL_FILE_PATH);
            String contentHtml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            String updatedContent = contentHtml.replace(INITIAL_CODE, code);

            SendEmailRequest request = generateSendEmailRequest(to, updatedContent);
            amazonSimpleEmailService.sendEmail(request);
        } catch (IOException e) {
            log.error("이메일 생성 중 문제가 발생했습니다. error message = {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private SendEmailRequest generateSendEmailRequest(String to, String content) {
        Destination destination = new Destination()
                .withToAddresses(to);

        Message message = new Message()
                .withSubject(createContent(TITLE))
                .withBody(new Body()
                        .withHtml(createContent(content))
                );

        return new SendEmailRequest()
                .withSource(FROM)
                .withDestination(destination)
                .withMessage(message);
    }

    private Content createContent(String text) {
        return new Content()
                .withCharset("UTF-8")
                .withData(text);
    }
}
