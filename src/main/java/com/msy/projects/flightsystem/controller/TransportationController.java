package com.msy.projects.flightsystem.controller;

import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.service.TransportationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transportations")
public class TransportationController {

    private final TransportationService transportationService;

    @Autowired
    public TransportationController(TransportationService transportationService) {
        this.transportationService = transportationService;
    }

    @GetMapping
    public ResponseEntity<List<TransportationDto>> getAllTransportations() {
        return ResponseEntity.ok(transportationService.getAllTransportations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportationDto> getTransportationById(@PathVariable Long id) {
        return ResponseEntity.ok(transportationService.getTransportationById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TransportationDto>> searchTransportations(
            @RequestParam String originCode,
            @RequestParam String destinationCode) {
        return ResponseEntity.ok(
                transportationService.getTransportationsByOriginAndDestination(originCode, destinationCode));
    }

    @GetMapping("/origin")
    public ResponseEntity<List<TransportationDto>> getTransportationsByOriginAndDate(
            @RequestParam String originCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                transportationService.getTransportationsByOriginAndDate(originCode, date));
    }

    @PostMapping
    public ResponseEntity<TransportationDto> createTransportation(@RequestBody TransportationDto transportationDto) {
        TransportationDto createdTransportation = transportationService.createTransportation(transportationDto);
        return new ResponseEntity<>(createdTransportation, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportationDto> updateTransportation(
            @PathVariable Long id,
            @RequestBody TransportationDto transportationDto) {
        return ResponseEntity.ok(transportationService.updateTransportation(id, transportationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransportation(@PathVariable Long id) {
        transportationService.deleteTransportation(id);
        return ResponseEntity.noContent().build();
    }
}
