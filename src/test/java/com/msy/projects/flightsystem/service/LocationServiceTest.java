package com.msy.projects.flightsystem.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msy.projects.flightsystem.dto.LocationDto;
import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.repository.LocationRepository;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;
    private LocationDto testLocationDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        testLocation = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        testLocationDto = new LocationDto(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
    }

    @Test
    void getAllLocations_ShouldReturnAllLocations() {
        // Arrange
        List<Location> locations = Arrays.asList(
            testLocation,
            new Location(2L, "John F. Kennedy Airport", "USA", "New York", "JFK")
        );
        when(locationRepository.findAll()).thenReturn(locations);

        // Act
        List<LocationDto> result = locationService.getAllLocations();

        // Assert
        assertEquals(2, result.size());
        assertEquals("IST", result.get(0).getLocationCode());
        assertEquals("JFK", result.get(1).getLocationCode());
        verify(locationRepository, times(1)).findAll();
    }

    @Test
    void getLocationById_WhenLocationExists_ShouldReturnLocation() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));

        // Act
        LocationDto result = locationService.getLocationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testLocationDto.getId(), result.getId());
        assertEquals(testLocationDto.getName(), result.getName());
        assertEquals(testLocationDto.getLocationCode(), result.getLocationCode());
        verify(locationRepository, times(1)).findById(1L);
    }

    @Test
    void getLocationById_WhenLocationDoesNotExist_ShouldThrowException() {
        // Arrange
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            locationService.getLocationById(999L);
        });

        assertEquals("Location not found with id: 999", exception.getMessage());
        verify(locationRepository, times(1)).findById(999L);
    }

    @Test
    void getLocationByCode_WhenLocationExists_ShouldReturnLocation() {
        // Arrange
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(testLocation));

        // Act
        LocationDto result = locationService.getLocationByCode("IST");

        // Assert
        assertNotNull(result);
        assertEquals(testLocationDto.getLocationCode(), result.getLocationCode());
        verify(locationRepository, times(1)).findByLocationCode("IST");
    }

    @Test
    void createLocation_WhenLocationCodeDoesNotExist_ShouldCreateLocation() {
        // Arrange
        LocationDto newLocationDto = new LocationDto(null, "New Airport", "Country", "City", "NEW");
        Location savedLocation = new Location(3L, "New Airport", "Country", "City", "NEW");
        
        when(locationRepository.existsByLocationCode("NEW")).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        // Act
        LocationDto result = locationService.createLocation(newLocationDto);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("NEW", result.getLocationCode());
        verify(locationRepository).existsByLocationCode("NEW");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocation_WhenLocationCodeExists_ShouldThrowException() {
        // Arrange
        LocationDto newLocationDto = new LocationDto(null, "Duplicate Airport", "Country", "City", "IST");
        when(locationRepository.existsByLocationCode("IST")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            locationService.createLocation(newLocationDto);
        });

        assertEquals("Location with code IST already exists", exception.getMessage());
        verify(locationRepository).existsByLocationCode("IST");
        verify(locationRepository, never()).save(any(Location.class));
    }

    @Test
    void updateLocation_WhenLocationExistsAndCodeNotChanged_ShouldUpdateLocation() {
        // Arrange
        LocationDto updateDto = new LocationDto(1L, "Updated Istanbul Airport", "Turkey", "Istanbul", "IST");
        Location existingLocation = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        Location updatedLocation = new Location(1L, "Updated Istanbul Airport", "Turkey", "Istanbul", "IST");
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(updatedLocation);

        // Act
        LocationDto result = locationService.updateLocation(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Istanbul Airport", result.getName());
        assertEquals("IST", result.getLocationCode());
        verify(locationRepository).findById(1L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void deleteLocation_WhenLocationExists_ShouldDeleteLocation() {
        // Arrange
        when(locationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        
        // Act
        locationService.deleteLocation(1L);
        
        // Assert
        verify(locationRepository).findById(1L);
        verify(locationRepository).delete(testLocation);
    }

    @Test
    void deleteLocation_WhenLocationDoesNotExist_ShouldThrowException() {
        // Arrange
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            locationService.deleteLocation(999L);
        });
        
        assertEquals("Location not found with id: 999", exception.getMessage());
        verify(locationRepository).findById(999L);
        verify(locationRepository, never()).delete(any(Location.class));
    }
}
