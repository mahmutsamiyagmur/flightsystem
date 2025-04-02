package com.msy.projects.flightsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequestDto {
    private String originLocationCode;
    private String destinationLocationCode;
    private LocalDate travelDate;
}
