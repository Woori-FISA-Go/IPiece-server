package com.masterpiece.IPiece.user.api;

import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.user.api.dto.request.SignUpRequest;
import com.masterpiece.IPiece.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/signup")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signUp(
            @RequestPart("request") SignUpRequest request,
            @RequestPart("id_card") MultipartFile idCardFile
    ) {
        userService.signUp(request, idCardFile);
        return Responses.ok("회원가입 완료");
    }
}
