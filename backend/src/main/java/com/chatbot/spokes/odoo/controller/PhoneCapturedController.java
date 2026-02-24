package com.chatbot.spokes.odoo.controller;

import com.chatbot.spokes.odoo.model.FbCapturedPhone;
import com.chatbot.spokes.odoo.service.FbCapturedPhoneService; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/odoo/phone-captured")
@RequiredArgsConstructor
public class PhoneCapturedController {

    private final FbCapturedPhoneService capturedPhoneService; 

    /**
     * Lấy danh sách các số điện thoại theo owner (Principal)
     */
    @GetMapping
    public ResponseEntity<List<FbCapturedPhone>> getPhonesByOwner(Principal principal) {
        String ownerId = principal.getName(); 
        log.info("Fetching captured phone records for ownerId: {}", ownerId);

        // ✅ GỌI ĐÚNG TÊN PHƯƠNG THỨC TRONG SERVICE
        List<FbCapturedPhone> phones = capturedPhoneService.getAllPhoneRecordsByOwner(ownerId); 

        return ResponseEntity.ok(phones);
    }
}