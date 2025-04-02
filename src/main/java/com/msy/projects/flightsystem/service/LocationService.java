package com.msy.projects.flightsystem.service;

import com.msy.projects.flightsystem.dto.LocationDto;
import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public LocationDto getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        return mapToDto(location);
    }

    public LocationDto getLocationByCode(String locationCode) {
        Location location = locationRepository.findByLocationCode(locationCode)
                .orElseThrow(() -> new RuntimeException("Location not found with code: " + locationCode));
        return mapToDto(location);
    }

    @Transactional
    public LocationDto createLocation(LocationDto locationDto) {
        if (locationRepository.existsByLocationCode(locationDto.getLocationCode())) {
            throw new RuntimeException("Location with code " + locationDto.getLocationCode() + " already exists");
        }
        
        Location location = mapToEntity(locationDto);
        Location savedLocation = locationRepository.save(location);
        return mapToDto(savedLocation);
    }

    @Transactional
    public LocationDto updateLocation(Long id, LocationDto locationDto) {
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        
        // Check if trying to update to an existing code (that's not the current one)
        if (!existingLocation.getLocationCode().equals(locationDto.getLocationCode()) && 
                locationRepository.existsByLocationCode(locationDto.getLocationCode())) {
            throw new RuntimeException("Location with code " + locationDto.getLocationCode() + " already exists");
        }
        
        existingLocation.setName(locationDto.getName());
        existingLocation.setCity(locationDto.getCity());
        existingLocation.setCountry(locationDto.getCountry());
        existingLocation.setLocationCode(locationDto.getLocationCode());
        
        Location updatedLocation = locationRepository.save(existingLocation);
        return mapToDto(updatedLocation);
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        locationRepository.delete(location);
    }

    private LocationDto mapToDto(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getCountry(),
                location.getCity(),
                location.getLocationCode()
        );
    }

    private Location mapToEntity(LocationDto locationDto) {
        return new Location(
                locationDto.getId(),
                locationDto.getName(),
                locationDto.getCountry(),
                locationDto.getCity(),
                locationDto.getLocationCode()
        );
    }
}
