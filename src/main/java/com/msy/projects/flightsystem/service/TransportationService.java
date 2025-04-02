package com.msy.projects.flightsystem.service;

import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.model.Transportation;
import com.msy.projects.flightsystem.repository.LocationRepository;
import com.msy.projects.flightsystem.repository.TransportationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransportationService {

    private final TransportationRepository transportationRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public TransportationService(TransportationRepository transportationRepository,
                               LocationRepository locationRepository) {
        this.transportationRepository = transportationRepository;
        this.locationRepository = locationRepository;
    }

    public List<TransportationDto> getAllTransportations() {
        return transportationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public TransportationDto getTransportationById(Long id) {
        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transportation not found with id: " + id));
        return mapToDto(transportation);
    }

    public List<TransportationDto> getTransportationsByOriginAndDestination(String originCode, String destCode) {
        Location origin = locationRepository.findByLocationCode(originCode)
                .orElseThrow(() -> new RuntimeException("Origin location not found with code: " + originCode));
        
        Location destination = locationRepository.findByLocationCode(destCode)
                .orElseThrow(() -> new RuntimeException("Destination location not found with code: " + destCode));
        
        return transportationRepository.findByOriginLocationAndDestinationLocation(origin, destination).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TransportationDto> getTransportationsByOriginAndDate(String originCode, LocalDate date) {
        Location origin = locationRepository.findByLocationCode(originCode)
                .orElseThrow(() -> new RuntimeException("Origin location not found with code: " + originCode));
        
        // Get the day of week (1=Monday, 2=Tuesday, etc.)
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Integer dayValue = dayOfWeek.getValue();
        
        return transportationRepository.findByOriginLocationAndOperatingDay(origin, dayValue).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportationDto createTransportation(TransportationDto transportationDto) {
        Location origin = locationRepository.findById(transportationDto.getOriginLocationId())
                .orElseThrow(() -> new RuntimeException("Origin location not found with id: " + transportationDto.getOriginLocationId()));
        
        Location destination = locationRepository.findById(transportationDto.getDestinationLocationId())
                .orElseThrow(() -> new RuntimeException("Destination location not found with id: " + transportationDto.getDestinationLocationId()));
        
        Transportation transportation = new Transportation();
        transportation.setOriginLocation(origin);
        transportation.setDestinationLocation(destination);
        transportation.setTransportationType(transportationDto.getTransportationType());
        transportation.setOperatingDays(transportationDto.getOperatingDays());
        
        Transportation savedTransportation = transportationRepository.save(transportation);
        return mapToDto(savedTransportation);
    }

    @Transactional
    public TransportationDto updateTransportation(Long id, TransportationDto transportationDto) {
        Transportation existingTransportation = transportationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transportation not found with id: " + id));
        
        Location origin = locationRepository.findById(transportationDto.getOriginLocationId())
                .orElseThrow(() -> new RuntimeException("Origin location not found with id: " + transportationDto.getOriginLocationId()));
        
        Location destination = locationRepository.findById(transportationDto.getDestinationLocationId())
                .orElseThrow(() -> new RuntimeException("Destination location not found with id: " + transportationDto.getDestinationLocationId()));
        
        existingTransportation.setOriginLocation(origin);
        existingTransportation.setDestinationLocation(destination);
        existingTransportation.setTransportationType(transportationDto.getTransportationType());
        existingTransportation.setOperatingDays(transportationDto.getOperatingDays());
        
        Transportation updatedTransportation = transportationRepository.save(existingTransportation);
        return mapToDto(updatedTransportation);
    }

    @Transactional
    public void deleteTransportation(Long id) {
        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transportation not found with id: " + id));
        transportationRepository.delete(transportation);
    }

    public TransportationDto mapToDto(Transportation transportation) {
        TransportationDto dto = new TransportationDto();
        dto.setId(transportation.getId());
        dto.setOriginLocationId(transportation.getOriginLocation().getId());
        dto.setOriginLocationCode(transportation.getOriginLocation().getLocationCode());
        dto.setDestinationLocationId(transportation.getDestinationLocation().getId());
        dto.setDestinationLocationCode(transportation.getDestinationLocation().getLocationCode());
        dto.setTransportationType(transportation.getTransportationType());
        dto.setOperatingDays(transportation.getOperatingDays());
        return dto;
    }
}
