package com.chatbot.spokes.facebook.autoconnect.controller;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.spokes.facebook.autoconnect.dto.CreateFbAutoConnectRequest;
import com.chatbot.spokes.facebook.autoconnect.service.FbAutoConnectService;
import com.chatbot.spokes.facebook.autoconnect.dto.AutoConnectResponse;
import com.chatbot.spokes.facebook.autoconnect.dto.AutoConnectClientRequest;
import com.chatbot.spokes.facebook.connection.service.FacebookConnectionService;
import com.chatbot.spokes.facebook.connection.dto.CreateFacebookConnectionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/connection/facebook/auto-connect")
@Slf4j
public class FbAutoConnectController {

  private final FbAutoConnectService fbAutoConnectService;
  private final FacebookConnectionService facebookConnectionService;

  public FbAutoConnectController(FbAutoConnectService fbAutoConnectService, FacebookConnectionService facebookConnectionService) {
    this.fbAutoConnectService = fbAutoConnectService;
    this.facebookConnectionService = facebookConnectionService;
  }

  @PostMapping("/auto")
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

  @PostMapping("/client")
  public ResponseEntity<AutoConnectResponse> autoConnectClient(@Valid @RequestBody AutoConnectClientRequest request,
                              Principal principal) {
    String ownerId = principal.getName();
    
    log.info("📩 Received auto-connect-client request for {} connections", request.getConnections().size());

    // Process each connection in the list
    int successCount = 0;
    int failureCount = 0;
    StringBuilder message = new StringBuilder();
    
    for (AutoConnectClientRequest.ConnectionData connectionData : request.getConnections()) {
      try {
        // Create Facebook connection request
        CreateFacebookConnectionRequest fbRequest = new CreateFacebookConnectionRequest();
        fbRequest.setBotId(connectionData.getBotId());
        fbRequest.setBotName(connectionData.getBotName());
        fbRequest.setPageId(connectionData.getPageId());
        fbRequest.setFanpageUrl(connectionData.getFanpageUrl());
        fbRequest.setUserAccessToken(connectionData.getUserAccessToken()); // ✅ Use userAccessToken for token exchange
        fbRequest.setEnabled(connectionData.getIsEnabled() != null ? connectionData.getIsEnabled() : true);
        
        // Use FacebookConnectionService to create connection
        facebookConnectionService.createConnection(ownerId, fbRequest);
        successCount++;
        
      } catch (Exception e) {
        log.error("Failed to create connection for page {}: {}", connectionData.getPageId(), e.getMessage());
        failureCount++;
        message.append("Failed to connect page ").append(connectionData.getPageId()).append(": ").append(e.getMessage()).append("; ");
      }
    }
    
    // Build response
    String finalMessage = message.length() > 0 ? message.toString() : 
      String.format("Successfully connected %d pages. %d pages failed.", successCount, failureCount);
    
    AutoConnectResponse result = new AutoConnectResponse();
    result.setSuccess(failureCount == 0);
    result.setMessage(finalMessage);
    
    return ResponseEntity.ok(result);
  }
}