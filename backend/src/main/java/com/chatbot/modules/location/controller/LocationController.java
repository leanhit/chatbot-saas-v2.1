package com.chatbot.modules.location.controller;

import com.chatbot.modules.location.dto.DistrictDTO;
import com.chatbot.modules.location.dto.ProvinceDTO;
import com.chatbot.modules.location.dto.WardDTO;
import com.chatbot.modules.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/provinces")
    public List<ProvinceDTO> getProvinces() {
        return locationService.getProvinces();
    }

    @GetMapping("/districts")
    public List<DistrictDTO> getDistricts(
            @RequestParam int provinceCode
    ) {
        return locationService.getDistricts(provinceCode);
    }

    @GetMapping("/wards")
    public List<WardDTO> getWards(
            @RequestParam int districtCode
    ) {
        return locationService.getWards(districtCode);
    }
}
