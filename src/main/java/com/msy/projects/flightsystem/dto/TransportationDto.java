package com.msy.projects.flightsystem.dto;

import com.msy.projects.flightsystem.model.TransportationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportationDto {
    private Long id;
    private Long originLocationId;
    private String originLocationCode;
    private Long destinationLocationId;
    private String destinationLocationCode;
    private TransportationType transportationType;
    private List<Integer> operatingDays;
}
