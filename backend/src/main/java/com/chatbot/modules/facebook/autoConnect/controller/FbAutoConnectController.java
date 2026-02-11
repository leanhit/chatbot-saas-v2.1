package com.chatbot.modules.facebook.autoConnect.controller;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.modules.facebook.autoConnect.dto.CreateFbAutoConnectRequest;
import com.chatbot.modules.facebook.autoConnect.service.FbAutoConnectService;
import com.chatbot.modules.facebook.autoConnect.dto.AutoConnectResponse; // Thêm import mới
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/connection/facebook/auto-connect")
@Slf4j
public class FbAutoConnectController {

  private final FbAutoConnectService fbAutoConnectService;

  public FbAutoConnectController(FbAutoConnectService fbAutoConnectService) {
    this.fbAutoConnectService = fbAutoConnectService;
  }

  @PostMapping
  public ResponseEntity<AutoConnectResponse> autoConnect(@Valid @RequestBody CreateFbAutoConnectRequest request,
                          Principal principal) {
    String ownerId = principal.getName();

    log.info("📩 Received auto-connect request: " + request);

    // Thay đổi kiểu dữ liệu nhận về
    AutoConnectResponse result = fbAutoConnectService.autoConnect(
        ownerId,
        request.getBotId(),
        request.getUserAccessToken()
    );

    // Trả về đối tượng kết quả chi tiết
    return ResponseEntity.ok(result);
  }
}