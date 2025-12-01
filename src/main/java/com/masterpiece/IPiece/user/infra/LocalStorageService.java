package com.masterpiece.IPiece.user.infra;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //CDN 지원을 위한 설정 추가
    @Value("${cloud.aws.cdn.url:}")
    private String cdnUrl;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public String saveIdCard(MultipartFile file, String userId) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
            }

            String ext = extractExt(originalName);
            LocalDate now = LocalDate.now();

            // 🔹 신분증: id-card/{year}/{month}/{userId}/idcard_{uuid}.ext
            String fileName = buildFileName("idcard_", ext);
            String key = String.format(
                    "id-card/%d/%02d/%s/%s",
                    now.getYear(),
                    now.getMonthValue(),
                    userId,
                    fileName
            );

            uploadToS3(file, key);
            return getFileUrl(key);  // CDN 또는 S3 URL 반환

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    @Override
    public String saveProductImage(MultipartFile file, String category, String productKey) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
            }

            String ext = extractExt(originalName);
            LocalDate now = LocalDate.now();

            // 🔹 상품 이미지: products/{category}/{year}/{month}/{productKey}/{uuid}.ext
            String fileName = buildFileName("img_", ext);
            String key = String.format(
                    "products/%s/%d/%02d/%s/%s",
                    category,
                    now.getYear(),
                    now.getMonthValue(),
                    productKey,
                    fileName
            );

            uploadToS3(file, key);
            return getFileUrl(key);  // CDN 또는 S3 URL 반환

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private void uploadToS3(MultipartFile file, String key) throws Exception {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucket, key, in, metadata);
        }
    }

    /**
     * CloudFront CDN이 설정되어 있으면 CDN URL을,
     * 없으면 S3 직접 URL을 반환
     */
    private String getFileUrl(String key) {
        if (cdnUrl != null && !cdnUrl.isEmpty()) {
            return cdnUrl + "/" + key;
        }
        return amazonS3.getUrl(bucket, key).toString();
    }

    private String extractExt(String originalName) {
        int dotIdx = originalName.lastIndexOf('.');
        if (dotIdx >= 0) {
            return originalName.substring(dotIdx + 1);
        }
        return "";
    }

    private String buildFileName(String prefix, String ext) {
        if (ext == null || ext.isBlank()) {
            return prefix + UUID.randomUUID();
        }
        return prefix + UUID.randomUUID() + "." + ext;
    }
}