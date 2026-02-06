package com.chatbot.modules.address.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Embedded Address Object - Địa chỉ nhúng trong User/Tenant
 * Thay vì lưu mảng addresses, mỗi User/Tenant sẽ có 1 address object
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEmbedded {

    @Column(name = "house_number", length = 50)
    private String houseNumber;

    @Column(length = 255)
    private String street;

    @Column(length = 255)
    private String ward;

    @Column(length = 255)
    private String district;

    @Column(length = 255)
    private String province;

    @Column(length = 100)
    private String country;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "full_address", length = 500)
    private String fullAddress;

    @PrePersist
    @PreUpdate
    protected void generateFullAddress() {
        if (houseNumber != null && street != null && ward != null && 
            district != null && province != null && country != null) {
            fullAddress = String.format("%s %s, %s, %s, %s, %s", 
                houseNumber, street, ward, district, province, country);
        }
    }
}
