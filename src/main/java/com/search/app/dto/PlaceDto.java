package com.search.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDto {
    private String title;
    private String address;
    private String roadAddress;
    private String longitude;
    private String latitude;
    private String categoryName;
    private String phone;
}