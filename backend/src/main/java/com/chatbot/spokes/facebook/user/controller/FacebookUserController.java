package com.chatbot.spokes.facebook.user.controller;

import com.chatbot.spokes.facebook.user.dto.FacebookUserInfo;
import com.chatbot.spokes.facebook.user.model.FacebookUser;
import com.chatbot.spokes.facebook.user.service.FacebookUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Facebook User", description = "Quản lý thông tin người dùng Facebook")
@RestController
@RequestMapping("/api/v1/facebook/users")
@RequiredArgsConstructor
public class FacebookUserController {

    private final FacebookUserService facebookUserService;

    @Operation(summary = "Lấy thông tin người dùng bằng PSID")
    @GetMapping("/{psid}")
    public ResponseEntity<FacebookUserInfo> getUserByPsid(
            @PathVariable String psid,
            @RequestParam String pageId) {
        return ResponseEntity.ok(facebookUserService.getUserInfo(psid, pageId));
    }

    @Operation(summary = "Tìm kiếm người dùng")
    @GetMapping("/search")
    public ResponseEntity<Page<FacebookUserInfo>> searchUsers(
            @RequestParam String pageId,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(facebookUserService.searchUsers(pageId, keyword, pageable));
    }

    @Operation(summary = "Cập nhật thông tin Odoo Partner ID")
    @PutMapping("/{psid}/odoo-partner")
    public ResponseEntity<FacebookUserInfo> updateOdooPartnerId(
            @PathVariable String psid,
            @RequestParam String pageId,
            @RequestParam Integer odooPartnerId) {
        return ResponseEntity.ok(facebookUserService.updateOdooPartnerId(psid, pageId, odooPartnerId));
    }

    @Operation(summary = "Đồng bộ thông tin từ Facebook")
    @PostMapping("/{psid}/sync")
    public ResponseEntity<FacebookUserInfo> syncWithFacebook(
            @PathVariable String psid,
            @RequestParam String pageId) {
        return ResponseEntity.ok(facebookUserService.syncWithFacebook(psid, pageId));
    }

    @Operation(summary = "Lấy danh sách người dùng chưa được map với Odoo")
    @GetMapping("/unmapped")
    public ResponseEntity<Page<FacebookUserInfo>> getUnmappedUsers(
            @RequestParam String pageId,
            Pageable pageable) {
        return ResponseEntity.ok(facebookUserService.findUnmappedUsers(pageId, pageable));
    }

    @Operation(summary = "Lấy thông tin người dùng bằng Odoo Partner ID")
    @GetMapping("/odoo/{odooPartnerId}")
    public ResponseEntity<FacebookUserInfo> getUserByOdooId(
            @PathVariable Integer odooPartnerId,
            @RequestParam String pageId) {
        return ResponseEntity.ok(facebookUserService.findByOdooPartnerId(odooPartnerId, pageId));
    }

    @Operation(summary = "Cập nhật thông tin đăng ký nhận tin nhắn")
    @PutMapping("/{psid}/subscription")
    public ResponseEntity<Void> updateSubscription(
            @PathVariable String psid,
            @RequestParam String pageId,
            @RequestParam boolean isSubscribed) {
        facebookUserService.updateSubscription(psid, pageId, isSubscribed);
        return ResponseEntity.ok().build();
    }
}
