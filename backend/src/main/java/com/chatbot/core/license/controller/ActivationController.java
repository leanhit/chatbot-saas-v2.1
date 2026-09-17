package com.chatbot.core.license.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActivationController {

    @GetMapping("/activate")
    public ResponseEntity<Void> activatePage(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String token) {
        if (deviceId != null && state != null) {
            String target = String.format("/api/license/activate?deviceId=%s&state=%s", deviceId, state);
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", target)
                .build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", "/activate.html")
            .build();
    }

    @GetMapping("/")
    public ResponseEntity<Void> homePage() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", "/activation-saas.html")
            .build();
    }
}
