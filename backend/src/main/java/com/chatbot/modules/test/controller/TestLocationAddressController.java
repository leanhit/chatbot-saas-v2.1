package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Test Location and Address Controller
 */
@RestController
@RequestMapping("/api/test/location-address")
public class TestLocationAddressController {
    
    /**
     * Test tạo address với đầy đủ các trường theo yêu cầu
     */
    @PostMapping("/address")
    public ResponseEntity<Map<String, Object>> createTestAddress(@RequestBody Map<String, Object> request) {
        String houseNumber = (String) request.get("houseNumber");
        String street = (String) request.get("street");
        String ward = (String) request.get("ward");
        String district = (String) request.get("district");
        String province = (String) request.get("province");
        String country = (String) request.getOrDefault("country", "Việt Nam");
        
        UUID addressId = UUID.randomUUID();
        
        Map<String, Object> address = new java.util.HashMap<>();
        address.put("id", addressId.toString());
        address.put("houseNumber", houseNumber);
        address.put("street", street);
        address.put("ward", ward);
        address.put("district", district);
        address.put("province", province);
        address.put("country", country);
        address.put("fullAddress", String.format("%s %s, %s, %s, %s, %s", 
            houseNumber, street, ward, district, province, country));
        address.put("isDefault", request.getOrDefault("isDefault", false));
        address.put("createdAt", System.currentTimeMillis());
        address.put("message", "Address created successfully with Vietnamese format");
        
        return ResponseEntity.ok(address);
    }
    
    /**
     * Test lấy danh sách provinces
     */
    @GetMapping("/provinces")
    public ResponseEntity<Map<String, Object>> getProvinces() {
        Map<String, Object> response = Map.of(
            "provinces", java.util.List.of(
                Map.of("code", 79, "name", "Thành phố Hồ Chí Minh", "type", "thành phố trung ương"),
                Map.of("code", 01, "name", "Thành phố Hà Nội", "type", "thành phố trung ương"),
                Map.of("code", 48, "name", "Đà Nẵng", "type", "thành phố trung ương"),
                Map.of("code", 92, "name", "Cần Thơ", "type", "thành phố trung ương")
            ),
            "total", 4,
            "message", "Provinces retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test lấy districts theo province
     */
    @GetMapping("/districts")
    public ResponseEntity<Map<String, Object>> getDistricts(@RequestParam int provinceCode) {
        String provinceName = "";
        
        if (provinceCode == 79) {
            provinceName = "Thành phố Hồ Chí Minh";
        } else if (provinceCode == 01) {
            provinceName = "Thành phố Hà Nội";
        }
        
        Map<String, Object> response = Map.of(
            "provinceCode", provinceCode,
            "provinceName", provinceName,
            "districts", provinceCode == 79 ? java.util.List.of(
                Map.of("code", 760, "name", "Quận 1", "type", "quận"),
                Map.of("code", 762, "name", "Quận 3", "type", "quận"),
                Map.of("code", 764, "name", "Quận 5", "type", "quận"),
                Map.of("code", 770, "name", "Quận 10", "type", "quận")
            ) : java.util.List.of(),
            "total", provinceCode == 79 ? 4 : 0,
            "message", "Districts retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test lấy wards theo district
     */
    @GetMapping("/wards")
    public ResponseEntity<Map<String, Object>> getWards(@RequestParam int districtCode) {
        String districtName = "";
        
        if (districtCode == 760) {
            districtName = "Quận 1";
        } else if (districtCode == 762) {
            districtName = "Quận 3";
        }
        
        Map<String, Object> response = Map.of(
            "districtCode", districtCode,
            "districtName", districtName,
            "wards", districtCode == 760 ? java.util.List.of(
                Map.of("code", 26814, "name", "Phường Bến Thành", "type", "phường"),
                Map.of("code", 26815, "name", "Phường Bến Nghé", "type", "phường"),
                Map.of("code", 26816, "name", "Phường Cầu Kho", "type", "phường"),
                Map.of("code", 26817, "name", "Phường Cầu Ông Lãnh", "type", "phường")
            ) : java.util.List.of(),
            "total", districtCode == 760 ? 4 : 0,
            "message", "Wards retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test tạo address với location codes
     */
    @PostMapping("/address-with-codes")
    public ResponseEntity<Map<String, Object>> createAddressWithCodes(@RequestBody Map<String, Object> request) {
        Integer provinceCode = (Integer) request.get("provinceCode");
        Integer districtCode = (Integer) request.get("districtCode");
        Integer wardCode = (Integer) request.get("wardCode");
        String houseNumber = (String) request.get("houseNumber");
        String street = (String) request.get("street");
        
        // Mock location names based on codes
        String provinceName = provinceCode == 79 ? "Thành phố Hồ Chí Minh" : "Unknown Province";
        String districtName = districtCode == 760 ? "Quận 1" : "Unknown District";
        String wardName = wardCode == 26814 ? "Phường Bến Thành" : "Unknown Ward";
        
        UUID addressId = UUID.randomUUID();
        
        Map<String, Object> address = new java.util.HashMap<>();
        address.put("id", addressId.toString());
        address.put("houseNumber", houseNumber);
        address.put("street", street);
        address.put("ward", wardName);
        address.put("district", districtName);
        address.put("province", provinceName);
        address.put("country", "Việt Nam");
        address.put("locationCodes", Map.of(
            "provinceCode", provinceCode,
            "districtCode", districtCode,
            "wardCode", wardCode
        ));
        address.put("fullAddress", String.format("%s %s, %s, %s, %s, Việt Nam", 
            houseNumber, street, wardName, districtName, provinceName));
        address.put("createdAt", System.currentTimeMillis());
        address.put("message", "Address created with location codes successfully");
        
        return ResponseEntity.ok(address);
    }
}
