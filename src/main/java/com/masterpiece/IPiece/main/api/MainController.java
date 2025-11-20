package com.masterpiece.IPiece.main.api;

import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.main.api.dto.response.MainPageResponse;
import com.masterpiece.IPiece.main.application.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping("/home")
    public ResponseEntity<?> getMainPage(
            @AuthenticationPrincipal Long userId
    ){

        MainPageResponse response = mainService.getMainPage(userId);

        return Responses.ok(response);
    }
}
