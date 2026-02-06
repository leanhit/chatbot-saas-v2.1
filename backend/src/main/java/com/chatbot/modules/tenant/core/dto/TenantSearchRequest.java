package com.chatbot.modules.tenant.core.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class TenantSearchRequest {
    private String keyword;
    private int page = 0;
    private int size = 10;
    private String sortBy = "name";
    private String sortDirection = "asc";

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
