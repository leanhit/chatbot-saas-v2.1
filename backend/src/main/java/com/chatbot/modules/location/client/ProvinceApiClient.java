package com.chatbot.modules.location.client;

import com.chatbot.modules.location.dto.DistrictDTO;
import com.chatbot.modules.location.dto.DistrictWithWardsDTO;
import com.chatbot.modules.location.dto.ProvinceDTO;
import com.chatbot.modules.location.dto.ProvinceWithDistrictsDTO;
import com.chatbot.modules.location.dto.WardDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
public class ProvinceApiClient {

    private static final String BASE_URL = "https://provinces.open-api.vn/api";

    private final RestTemplate restTemplate;

    public ProvinceApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<ProvinceDTO> getProvinces() {
        ProvinceDTO[] result =
                restTemplate.getForObject(BASE_URL + "/p/", ProvinceDTO[].class);
        return List.of(Objects.requireNonNull(result));
    }

    public List<DistrictDTO> getDistricts(int provinceCode) {
        ProvinceWithDistrictsDTO response =
                restTemplate.getForObject(
                        BASE_URL + "/p/" + provinceCode + "?depth=2",
                        ProvinceWithDistrictsDTO.class
                );
        return response != null ? response.getDistricts() : List.of();
    }

    public List<WardDTO> getWards(int districtCode) {
        DistrictWithWardsDTO response =
                restTemplate.getForObject(
                        BASE_URL + "/d/" + districtCode + "?depth=2",
                        DistrictWithWardsDTO.class
                );
        return response != null ? response.getWards() : List.of();
    }
}
