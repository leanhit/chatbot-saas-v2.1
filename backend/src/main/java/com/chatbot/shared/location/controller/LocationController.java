package com.chatbot.shared.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatbot.shared.location.dto.ProvinceDto;
import com.chatbot.shared.location.dto.DistrictDto;
import com.chatbot.shared.location.dto.WardDto;
import com.chatbot.shared.location.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "Vietnam provinces, districts, and wards management")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/provinces")
    @Operation(
        summary = "Get all provinces",
        description = "Retrieve list of all Vietnamese provinces",
        responses = {
            @ApiResponse(responseCode = "200", description = "Provinces retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<List<ProvinceDto>> getProvinces() {
        return ResponseEntity.ok(locationService.getAllProvinces());
    }

    @GetMapping("/districts")
    @Operation(
        summary = "Get districts by province code",
        description = "Retrieve list of districts for a specific province",
        responses = {
            @ApiResponse(responseCode = "200", description = "Districts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid province code"),
            @ApiResponse(responseCode = "404", description = "Province not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<List<DistrictDto>> getDistricts(
            @RequestParam String provinceCode) {
        
        List<DistrictDto> districts = locationService.getDistrictsByProvinceCode(provinceCode);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/wards")
    @Operation(
        summary = "Get wards by district code",
        description = "Retrieve list of wards for a specific district",
        responses = {
            @ApiResponse(responseCode = "200", description = "Wards retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid district code"),
            @ApiResponse(responseCode = "404", description = "District not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    public ResponseEntity<List<WardDto>> getWards(
            @RequestParam String districtCode) {
        
        List<WardDto> wards = locationService.getWardsByDistrictCode(districtCode);
        return ResponseEntity.ok(wards);
    }
}
