package com.chatbot.shared.location.service;

import com.chatbot.shared.location.dto.ProvinceDto;
import com.chatbot.shared.location.dto.DistrictDto;
import com.chatbot.shared.location.dto.WardDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private List<ProvinceDto> provinces;
    private List<DistrictDto> districts;
    private List<WardDto> wards;
    private Map<String, List<DistrictDto>> districtsByProvince;
    private Map<String, List<WardDto>> wardsByDistrict;

    @PostConstruct
    public void initData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            
            // Load provinces
            InputStream provincesStream = getClass().getClassLoader()
                .getResourceAsStream("data/provinces.json");
            provinces = mapper.readValue(provincesStream, new TypeReference<List<ProvinceDto>>() {});
            
            // Load districts
            InputStream districtsStream = getClass().getClassLoader()
                .getResourceAsStream("data/districts.json");
            districts = mapper.readValue(districtsStream, new TypeReference<List<DistrictDto>>() {});
            
            // Load wards
            InputStream wardsStream = getClass().getClassLoader()
                .getResourceAsStream("data/wards.json");
            wards = mapper.readValue(wardsStream, new TypeReference<List<WardDto>>() {});
            
            // Create lookup maps
            districtsByProvince = districts.stream()
                .collect(Collectors.groupingBy(DistrictDto::getProvinceCode));
            
            wardsByDistrict = wards.stream()
                .collect(Collectors.groupingBy(WardDto::getDistrictCode));
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to load location data", e);
        }
    }

    public List<ProvinceDto> getAllProvinces() {
        return provinces;
    }

    public List<DistrictDto> getDistrictsByProvinceCode(String provinceCode) {
        return districtsByProvince.getOrDefault(provinceCode, new ArrayList<>());
    }

    public List<WardDto> getWardsByDistrictCode(String districtCode) {
        return wardsByDistrict.getOrDefault(districtCode, new ArrayList<>());
    }
}
