package com.masterpiece.IPiece.user.infra;

import org.springframework.web.multipart.MultipartFile;


public interface StorageService {
    String saveIdCard(MultipartFile file, String userId);
}
