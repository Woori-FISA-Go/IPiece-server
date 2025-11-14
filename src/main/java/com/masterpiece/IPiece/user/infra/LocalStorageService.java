package com.masterpiece.IPiece.user.infra;

// 개발용 Service ( 클라우드 배포시 S3로 변경 )

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService{

    //저장 경로
    private final Path basePath = Paths.get("src/main/resources/idcards");


    @Override
    public String saveIdCard(MultipartFile file, String userId) {
        try {
            //NPE 방지
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
            }
            //확장자 추출
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalName.substring(dotIdx + 1);
            }
            
            // 연도/월/userId 구조로 폴더 생성
            String folder = String.format("%s/%s/%s",
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonthValue(),
                    userId
            );

            // 저장 경로 생성
            Path userDir = basePath.resolve(folder);
            Files.createDirectories(userDir);

            // 저장 파일명 생성
            String fileName = "idcard_" + UUID.randomUUID() + "." + ext;

            // 최종 파일 경로
            Path filePath = userDir.resolve(fileName);

            //파일 저장(이미 존재하면 덮어쓰는 구조)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 로컬 저장 경로 리턴 (DB 저장용)
            return filePath.toString();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }
}
