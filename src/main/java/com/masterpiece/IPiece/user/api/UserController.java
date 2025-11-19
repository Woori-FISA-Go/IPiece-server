package com.masterpiece.IPiece.user.api;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.user.api.dto.request.SignUpRequest;
import com.masterpiece.IPiece.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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

    @PostMapping("/duplicate-check")
    public ResponseEntity<?> duplicateCheck(@RequestParam("id") String userMadeId) {

        boolean available = userService.duplicateCheck(userMadeId);

        return Responses.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."
        ));
    }

}
