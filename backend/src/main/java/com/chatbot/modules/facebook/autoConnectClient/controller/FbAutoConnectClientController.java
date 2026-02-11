// src/main/java/com/chatbot/chatHub/facebook/autoConnectClient/controller/FacebookAutoConnectController.java

package com.chatbot.modules.facebook.autoConnectClient.controller;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.modules.facebook.autoConnectClient.dto.CreateFbAutoConnectClientRequest;
import com.chatbot.modules.facebook.autoConnectClient.service.FbAutoConnectClientService; // Sửa tên class Service
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/connection/facebook/auto-connect-client")
@Slf4j
public class FbAutoConnectClientController {

    private final FbAutoConnectClientService fbAutoConnectClientService; // Sửa tên biến

    public FbAutoConnectClientController(FbAutoConnectClientService fbAutoConnectClientService) {
        this.fbAutoConnectClientService = fbAutoConnectClientService;
    }

    @PostMapping
    public ResponseEntity<List<String>> createBulkConnections(@Valid @RequestBody CreateFbAutoConnectClientRequest request, Principal principal) {
        // Dòng code được thêm để in dữ liệu request
        //log.info("Received request body: " + request);
        
        String ownerId = principal.getName();
        List<String> connectionIds = fbAutoConnectClientService.createConnections(ownerId, request);
        return ResponseEntity.ok(connectionIds);
    }     
}