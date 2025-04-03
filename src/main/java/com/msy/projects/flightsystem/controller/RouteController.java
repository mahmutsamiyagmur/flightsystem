package com.msy.projects.flightsystem.controller;

import com.msy.projects.flightsystem.dto.RouteRequestDto;
import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }
    

    @GetMapping("/search")
    public ResponseEntity<List<List<TransportationDto>>> searchRoutes(
            @RequestParam String originCode,
            @RequestParam String destinationCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate) {
        return ResponseEntity.ok(
                routeService.findRoutes(originCode, destinationCode, travelDate)
        );
    }
}
