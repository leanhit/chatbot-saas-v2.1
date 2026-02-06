package com.chatbot.modules.location.service;

import com.chatbot.modules.location.client.ProvinceApiClient;
import com.chatbot.modules.location.dto.DistrictDTO;
import com.chatbot.modules.location.dto.ProvinceDTO;
import com.chatbot.modules.location.dto.WardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final ProvinceApiClient apiClient;

    @Cacheable("provinces")
    public List<ProvinceDTO> getProvinces() {
        return apiClient.getProvinces();
    }

    @Cacheable(value = "districts", key = "#provinceCode")
    public List<DistrictDTO> getDistricts(int provinceCode) {
        return apiClient.getDistricts(provinceCode);
    }

    @Cacheable(value = "wards", key = "#districtCode")
    public List<WardDTO> getWards(int districtCode) {
        return apiClient.getWards(districtCode);
    }
}
