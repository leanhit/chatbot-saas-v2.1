package com.chatbot.modules.location.dto;

import lombok.Data;
import java.util.List;

@Data
public class DistrictWithWardsDTO {
    private int code;
    private String name;
    private List<WardDTO> wards;
}
