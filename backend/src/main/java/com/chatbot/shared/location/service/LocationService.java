package com.chatbot.shared.location.service;

import com.chatbot.shared.location.dto.ProvinceDto;
import com.chatbot.shared.location.dto.DistrictDto;
import com.chatbot.shared.location.dto.WardDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    public List<ProvinceDto> getAllProvinces() {
        List<ProvinceDto> provinces = new ArrayList<>();
        
        // Add major Vietnamese provinces/cities
        provinces.add(ProvinceDto.builder()
                .code("01")
                .name("Thành phố Hà Nội")
                .nameEn("Hanoi")
                .fullName("Thành phố Hà Nội")
                .fullNameEn("Hanoi City")
                .codeName("thanh-pho-ha-noi")
                .administrativeUnit("Thành phố Trung ương")
                .region("Đồng bằng sông Hồng")
                .build());
                
        provinces.add(ProvinceDto.builder()
                .code("79")
                .name("Thành phố Hồ Chí Minh")
                .nameEn("Ho Chi Minh City")
                .fullName("Thành phố Hồ Chí Minh")
                .fullNameEn("Ho Chi Minh City")
                .codeName("thanh-pho-ho-chi-minh")
                .administrativeUnit("Thành phố Trung ương")
                .region("Đông Nam Bộ")
                .build());
                
        provinces.add(ProvinceDto.builder()
                .code("48")
                .name("Thành phố Đà Nẵng")
                .nameEn("Da Nang")
                .fullName("Thành phố Đà Nẵng")
                .fullNameEn("Da Nang City")
                .codeName("thanh-pho-da-nang")
                .administrativeUnit("Thành phố Trung ương")
                .region("Duyên hải Nam Trung Bộ")
                .build());
                
        provinces.add(ProvinceDto.builder()
                .code("92")
                .name("Thành phố Cần Thơ")
                .nameEn("Can Tho")
                .fullName("Thành phố Cần Thơ")
                .fullNameEn("Can Tho City")
                .codeName("thanh-pho-can-tho")
                .administrativeUnit("Thành phố Trung ương")
                .region("Đồng bằng sông Cửu Long")
                .build());
                
        provinces.add(ProvinceDto.builder()
                .code("31")
                .name("Thành phố Hải Phòng")
                .nameEn("Hai Phong")
                .fullName("Thành phố Hải Phòng")
                .fullNameEn("Hai Phong City")
                .codeName("thanh-pho-hai-phong")
                .administrativeUnit("Thành phố Trung ương")
                .region("Đồng bằng sông Hồng")
                .build());
                
        provinces.add(ProvinceDto.builder()
                .code("43")
                .name("Thành phố Đà Nẵng")
                .nameEn("Da Nang")
                .fullName("Thành phố Đà Nẵng")
                .fullNameEn("Da Nang City")
                .codeName("thanh-pho-da-nang")
                .administrativeUnit("Thành phố Trung ương")
                .region("Duyên hải Nam Trung Bộ")
                .build());
        
        // Add more provinces
        provinces.add(ProvinceDto.builder()
                .code("30")
                .name("Thành phố Hải Phòng")
                .nameEn("Hai Phong")
                .fullName("Thành phố Hải Phòng")
                .fullNameEn("Hai Phong City")
                .codeName("thanh-pho-hai-phong")
                .administrativeUnit("Thành phố Trung ương")
                .region("Đồng bằng sông Hồng")
                .build());
                
        return provinces;
    }

    public List<DistrictDto> getDistrictsByProvinceCode(String provinceCode) {
        List<DistrictDto> districts = new ArrayList<>();
        
        if ("01".equals(provinceCode)) { // Hà Nội
            districts.add(DistrictDto.builder()
                    .code("001")
                    .name("Quận Ba Đình")
                    .nameEn("Ba Dinh")
                    .fullName("Quận Ba Đình")
                    .fullNameEn("Ba Dinh District")
                    .codeName("quan-ba-dinh")
                    .provinceCode("01")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("002")
                    .name("Quận Hoàn Kiếm")
                    .nameEn("Hoan Kiem")
                    .fullName("Quận Hoàn Kiếm")
                    .fullNameEn("Hoan Kiem District")
                    .codeName("quan-hoan-kiem")
                    .provinceCode("01")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("003")
                    .name("Quận Tây Hồ")
                    .nameEn("Tay Ho")
                    .fullName("Quận Tây Hồ")
                    .fullNameEn("Tay Ho District")
                    .codeName("quan-tay-ho")
                    .provinceCode("01")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("004")
                    .name("Quận Long Biên")
                    .nameEn("Long Bien")
                    .fullName("Quận Long Biên")
                    .fullNameEn("Long Bien District")
                    .codeName("quan-long-bien")
                    .provinceCode("01")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("005")
                    .name("Quận Cầu Giấy")
                    .nameEn("Cau Giay")
                    .fullName("Quận Cầu Giấy")
                    .fullNameEn("Cau Giay District")
                    .codeName("quan-cau-giay")
                    .provinceCode("01")
                    .administrativeUnit("Quận")
                    .build());
        } else if ("79".equals(provinceCode)) { // Hồ Chí Minh
            districts.add(DistrictDto.builder()
                    .code("760")
                    .name("Quận 1")
                    .nameEn("District 1")
                    .fullName("Quận 1")
                    .fullNameEn("District 1")
                    .codeName("quan-1")
                    .provinceCode("79")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("762")
                    .name("Quận 3")
                    .nameEn("District 3")
                    .fullName("Quận 3")
                    .fullNameEn("District 3")
                    .codeName("quan-3")
                    .provinceCode("79")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("764")
                    .name("Quận 5")
                    .nameEn("District 5")
                    .fullName("Quận 5")
                    .fullNameEn("District 5")
                    .codeName("quan-5")
                    .provinceCode("79")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("765")
                    .name("Quận 6")
                    .nameEn("District 6")
                    .fullName("Quận 6")
                    .fullNameEn("District 6")
                    .codeName("quan-6")
                    .provinceCode("79")
                    .administrativeUnit("Quận")
                    .build());
                    
            districts.add(DistrictDto.builder()
                    .code("766")
                    .name("Quận 7")
                    .nameEn("District 7")
                    .fullName("Quận 7")
                    .fullNameEn("District 7")
                    .codeName("quan-7")
                    .provinceCode("79")
                    .administrativeUnit("Quận")
                    .build());
        }
        
        return districts;
    }

    public List<WardDto> getWardsByDistrictCode(String districtCode) {
        List<WardDto> wards = new ArrayList<>();
        
        if ("001".equals(districtCode)) { // Ba Đình, Hà Nội
            wards.add(WardDto.builder()
                    .code("00001")
                    .name("Phường Phúc Xá")
                    .nameEn("Phuc Xa")
                    .fullName("Phường Phúc Xá")
                    .fullNameEn("Phuc Xa Ward")
                    .codeName("phuong-phuc-xa")
                    .districtCode("001")
                    .administrativeUnit("Phường")
                    .build());
                    
            wards.add(WardDto.builder()
                    .code("00003")
                    .name("Phường Trúc Bạch")
                    .nameEn("Truc Bach")
                    .fullName("Phường Trúc Bạch")
                    .fullNameEn("Truc Bach Ward")
                    .codeName("phuong-truc-bach")
                    .districtCode("001")
                    .administrativeUnit("Phường")
                    .build());
                    
            wards.add(WardDto.builder()
                    .code("00007")
                    .name("Phường Cống Vị")
                    .nameEn("Cong Vi")
                    .fullName("Phường Cống Vị")
                    .fullNameEn("Cong Vi Ward")
                    .codeName("phuong-cong-vi")
                    .districtCode("001")
                    .administrativeUnit("Phường")
                    .build());
        } else if ("760".equals(districtCode)) { // Quận 1, Hồ Chí Minh
            wards.add(WardDto.builder()
                    .code("00001")
                    .name("Phường Bến Nghé")
                    .nameEn("Ben Nghe")
                    .fullName("Phường Bến Nghé")
                    .fullNameEn("Ben Nghe Ward")
                    .codeName("phuong-ben-nge")
                    .districtCode("760")
                    .administrativeUnit("Phường")
                    .build());
                    
            wards.add(WardDto.builder()
                    .code("00002")
                    .name("Phường Bến Thành")
                    .nameEn("Ben Thanh")
                    .fullName("Phường Bến Thành")
                    .fullNameEn("Ben Thanh Ward")
                    .codeName("phuong-ben-thanh")
                    .districtCode("760")
                    .administrativeUnit("Phường")
                    .build());
                    
            wards.add(WardDto.builder()
                    .code("00003")
                    .name("Phường Nguyễn Cư Trinh")
                    .nameEn("Nguyen Cu Trinh")
                    .fullName("Phường Nguyễn Cư Trinh")
                    .fullNameEn("Nguyen Cu Trinh Ward")
                    .codeName("phuong-nguyen-cu-trinh")
                    .districtCode("760")
                    .administrativeUnit("Phường")
                    .build());
        }
        
        return wards;
    }
}
