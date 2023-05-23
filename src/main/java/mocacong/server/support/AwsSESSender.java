package mocacong.server.support;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AwsSESSender {

    private static final String FROM = "verify-email@mocacong.com";
    private static final String TITLE = "모카콩 이메일 인증";
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
        String contentHtml = EmailHtmlContent.verifyEmailHtmlContent;
        String updatedContent = contentHtml.replace(INITIAL_CODE, code);

        SendEmailRequest request = generateSendEmailRequest(to, updatedContent);
        amazonSimpleEmailService.sendEmail(request);
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
