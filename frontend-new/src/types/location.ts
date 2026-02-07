// types/location.ts

/**
 * Khớp hoàn toàn với các Location DTOs từ backend
 */

/**
 * Khớp với ProvinceDTO.java
 */
export interface ProvinceDTO {
  code: number;
  name: string;
}

/**
 * Khớp với DistrictDTO.java
 */
export interface DistrictDTO {
  code: number;
  name: string;
}

/**
 * Khớp với WardDTO.java
 */
export interface WardDTO {
  code: number;
  name: string;
}

/**
 * Khớp với DistrictWithWardsDTO.java
 */
export interface DistrictWithWardsDTO {
  code: number;
  name: string;
  wards: WardDTO[];
}

/**
 * Khớp với ProvinceWithDistrictsDTO.java
 */
export interface ProvinceWithDistrictsDTO {
  code: number;
  name: string;
  districts: DistrictDTO[];
}
