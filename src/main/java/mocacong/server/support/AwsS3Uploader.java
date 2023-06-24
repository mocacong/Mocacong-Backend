package mocacong.server.support;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mocacong.server.exception.badrequest.InvalidFileEmptyException;
import mocacong.server.service.event.DeleteNotUsedImagesEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Uploader {

    private static final String S3_BUCKET_DIRECTORY_NAME = "static";

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadImage(MultipartFile multipartFile) {
        checkInvalidUploadFile(multipartFile);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        String fileName = S3_BUCKET_DIRECTORY_NAME + "/" + UUID.randomUUID() + "." + multipartFile.getOriginalFilename();

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            log.error("S3 파일 업로드에 실패했습니다. {}", e.getMessage());
            throw new IllegalStateException("S3 파일 업로드에 실패했습니다.");
        }
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void checkInvalidUploadFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty() || multipartFile.getSize() == 0) {
            throw new InvalidFileEmptyException();
        }
    }

    @Async
    @EventListener
    public void deleteImages(DeleteNotUsedImagesEvent event) {
        List<String> imgUrls = event.getImgUrls();
        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        for (String imgUrl : imgUrls) {
            String fileName = S3_BUCKET_DIRECTORY_NAME + imgUrl.substring(imgUrl.lastIndexOf("/"));
            keys.add(new DeleteObjectsRequest.KeyVersion(fileName));
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(keys);
        DeleteObjectsResult result = amazonS3Client.deleteObjects(deleteObjectsRequest);
        log.info("Failed Delete Object Counts = {}", imgUrls.size() - result.getDeletedObjects().size());
    }
}
