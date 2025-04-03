package com.msy.projects.flightsystem.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.exception.ResourceNotFoundException;
import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.model.Transportation;
import com.msy.projects.flightsystem.model.TransportationType;
import com.msy.projects.flightsystem.repository.LocationRepository;
import com.msy.projects.flightsystem.repository.TransportationRepository;

@ExtendWith(MockitoExtension.class)
public class TransportationServiceTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private LocationRepository locationRepository;
    
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private TransportationService transportationService;

    private Location originLocation;
    private Location destinationLocation;
    private Transportation testTransportation;
    private TransportationDto testTransportationDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        originLocation = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        destinationLocation = new Location(2L, "John F. Kennedy Airport", "USA", "New York", "JFK");
        
        List<Integer> operatingDays = Arrays.asList(1, 3, 5, 7); // Monday, Wednesday, Friday, Sunday
        
        testTransportation = new Transportation();
        testTransportation.setId(1L);
        testTransportation.setOriginLocation(originLocation);
        testTransportation.setDestinationLocation(destinationLocation);
        testTransportation.setTransportationType(TransportationType.FLIGHT);
        testTransportation.setOperatingDays(operatingDays);
        
        testTransportationDto = new TransportationDto();
        testTransportationDto.setId(1L);
        testTransportationDto.setOriginLocationId(1L);
        testTransportationDto.setOriginLocationCode("IST");
        testTransportationDto.setDestinationLocationId(2L);
        testTransportationDto.setDestinationLocationCode("JFK");
        testTransportationDto.setTransportationType(TransportationType.FLIGHT);
        testTransportationDto.setOperatingDays(operatingDays);
    }

    @Test
    void getAllTransportations_ShouldReturnAllTransportations() {
        // Arrange
        List<Transportation> transportations = Arrays.asList(testTransportation);
        when(transportationRepository.findAll()).thenReturn(transportations);

        // Act
        List<TransportationDto> result = transportationService.getAllTransportations();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTransportationDto.getId(), result.get(0).getId());
        assertEquals(testTransportationDto.getOriginLocationCode(), result.get(0).getOriginLocationCode());
        verify(transportationRepository, times(1)).findAll();
    }

    @Test
    void getTransportationById_WhenTransportationExists_ShouldReturnTransportation() {
        // Arrange
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportation));

        // Act
        TransportationDto result = transportationService.getTransportationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTransportationDto.getId(), result.getId());
        assertEquals(testTransportationDto.getOriginLocationCode(), result.getOriginLocationCode());
        assertEquals(testTransportationDto.getDestinationLocationCode(), result.getDestinationLocationCode());
        verify(transportationRepository, times(1)).findById(1L);
    }

    @Test
    void getTransportationById_WhenTransportationDoesNotExist_ShouldThrowException() {
        // Arrange
        when(transportationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            transportationService.getTransportationById(999L);
        });

        assertEquals("Transportation not found with id: 999", exception.getMessage());
        verify(transportationRepository, times(1)).findById(999L);
    }

    @Test
    void getTransportationsByOriginAndDestination_ShouldReturnMatchingTransportations() {
        // Arrange
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(originLocation));
        when(locationRepository.findByLocationCode("JFK")).thenReturn(Optional.of(destinationLocation));
        when(transportationRepository.findByOriginLocationAndDestinationLocation(originLocation, destinationLocation))
            .thenReturn(Arrays.asList(testTransportation));

        // Act
        List<TransportationDto> result = transportationService.getTransportationsByOriginAndDestination("IST", "JFK");

        // Assert
        assertEquals(1, result.size());
        assertEquals("IST", result.get(0).getOriginLocationCode());
        assertEquals("JFK", result.get(0).getDestinationLocationCode());
        verify(transportationRepository, times(1))
            .findByOriginLocationAndDestinationLocation(originLocation, destinationLocation);
    }

    @Test
    void getTransportationsByOriginAndDate_ShouldReturnMatchingTransportations() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 4, 7); // Monday (day of week = 1)
        int dayOfWeek = date.getDayOfWeek().getValue();
        
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(originLocation));
        when(transportationRepository.findByOriginLocationAndOperatingDay(originLocation, dayOfWeek))
            .thenReturn(Arrays.asList(testTransportation));

        // Act
        List<TransportationDto> result = transportationService.getTransportationsByOriginAndDate("IST", date);

        // Assert
        assertEquals(1, result.size());
        assertEquals("IST", result.get(0).getOriginLocationCode());
        verify(transportationRepository, times(1))
            .findByOriginLocationAndOperatingDay(originLocation, dayOfWeek);
    }

    @Test
    void createTransportation_ShouldCreateTransportation() {
        // Arrange
        TransportationDto newTransportationDto = new TransportationDto();
        newTransportationDto.setOriginLocationId(1L);
        newTransportationDto.setDestinationLocationId(2L);
        newTransportationDto.setTransportationType(TransportationType.FLIGHT);
        newTransportationDto.setOperatingDays(Arrays.asList(1, 3, 5));
        
        Transportation newTransportation = new Transportation();
        newTransportation.setOriginLocation(originLocation);
        newTransportation.setDestinationLocation(destinationLocation);
        newTransportation.setTransportationType(TransportationType.FLIGHT);
        newTransportation.setOperatingDays(Arrays.asList(1, 3, 5));
        
        Transportation savedTransportation = new Transportation();
        savedTransportation.setId(2L);
        savedTransportation.setOriginLocation(originLocation);
        savedTransportation.setDestinationLocation(destinationLocation);
        savedTransportation.setTransportationType(TransportationType.FLIGHT);
        savedTransportation.setOperatingDays(Arrays.asList(1, 3, 5));
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(originLocation));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destinationLocation));
        when(transportationRepository.save(any(Transportation.class))).thenReturn(savedTransportation);

        // Act
        TransportationDto result = transportationService.createTransportation(newTransportationDto);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("IST", result.getOriginLocationCode());
        assertEquals("JFK", result.getDestinationLocationCode());
        verify(locationRepository).findById(1L);
        verify(locationRepository).findById(2L);
        verify(transportationRepository).save(any(Transportation.class));
        verify(cacheService).clearRouteCache();
    }

    @Test
    void updateTransportation_WhenTransportationExists_ShouldUpdateTransportation() {
        // Arrange
        TransportationDto updateDto = new TransportationDto();
        updateDto.setId(1L);
        updateDto.setOriginLocationId(1L);
        updateDto.setDestinationLocationId(2L);
        updateDto.setTransportationType(TransportationType.FLIGHT);
        updateDto.setOperatingDays(Arrays.asList(1, 2, 3, 4, 5, 6, 7)); // Updated to all days
        
        Transportation existingTransportation = testTransportation;
        Transportation updatedTransportation = new Transportation();
        updatedTransportation.setId(1L);
        updatedTransportation.setOriginLocation(originLocation);
        updatedTransportation.setDestinationLocation(destinationLocation);
        updatedTransportation.setTransportationType(TransportationType.FLIGHT);
        updatedTransportation.setOperatingDays(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(existingTransportation));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(originLocation));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(destinationLocation));
        when(transportationRepository.save(any(Transportation.class))).thenReturn(updatedTransportation);

        // Act
        TransportationDto result = transportationService.updateTransportation(1L, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(7, result.getOperatingDays().size()); // Should now have 7 operating days
        verify(transportationRepository).findById(1L);
        verify(transportationRepository).save(any(Transportation.class));
    }

    @Test
    void deleteTransportation_WhenTransportationExists_ShouldDeleteTransportation() {
        // Arrange
        when(transportationRepository.findById(1L)).thenReturn(Optional.of(testTransportation));
        
        // Act
        transportationService.deleteTransportation(1L);
        
        // Assert
        verify(transportationRepository).findById(1L);
        verify(transportationRepository).delete(testTransportation);
        verify(cacheService).clearRouteCache();
    }
}
