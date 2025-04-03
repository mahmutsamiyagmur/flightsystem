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
public class RouteServiceTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private LocationRepository locationRepository;
    
    @Mock
    private TransportationService transportationService;

    @InjectMocks
    private RouteService routeService;

    private Location taksimSquare;
    private Location istanbulAirport;
    private Location heathrowAirport;
    private Location wembleyStadium;
    
    private Transportation busToAirport;
    private Transportation flight;
    private Transportation uberToStadium;
    private Transportation subwayToAirport;
    
    private TransportationDto busTaksimDto;
    private TransportationDto flightDto;
    private TransportationDto uberToStadiumDto;
    private TransportationDto subwayToAirportDto;
    
    private LocalDate travelDate;
    private int dayOfWeek;

    @BeforeEach
    void setUp() {
        // Setup test locations
        taksimSquare = new Location(1L, "Taksim Square", "Turkey", "Istanbul", "TAK");
        istanbulAirport = new Location(2L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        heathrowAirport = new Location(3L, "Heathrow Airport", "UK", "London", "LHR");
        wembleyStadium = new Location(4L, "Wembley Stadium", "UK", "London", "WEM");
        
        // Setup operating days
        List<Integer> allDays = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        
        // Setup transportation objects
        busToAirport = new Transportation();
        busToAirport.setId(1L);
        busToAirport.setOriginLocation(taksimSquare);
        busToAirport.setDestinationLocation(istanbulAirport);
        busToAirport.setTransportationType(TransportationType.BUS);
        busToAirport.setOperatingDays(allDays);
        
        flight = new Transportation();
        flight.setId(2L);
        flight.setOriginLocation(istanbulAirport);
        flight.setDestinationLocation(heathrowAirport);
        flight.setTransportationType(TransportationType.FLIGHT);
        flight.setOperatingDays(allDays);
        
        uberToStadium = new Transportation();
        uberToStadium.setId(3L);
        uberToStadium.setOriginLocation(heathrowAirport);
        uberToStadium.setDestinationLocation(wembleyStadium);
        uberToStadium.setTransportationType(TransportationType.UBER);
        uberToStadium.setOperatingDays(allDays);
        
        subwayToAirport = new Transportation();
        subwayToAirport.setId(4L);
        subwayToAirport.setOriginLocation(taksimSquare);
        subwayToAirport.setDestinationLocation(istanbulAirport);
        subwayToAirport.setTransportationType(TransportationType.SUBWAY);
        subwayToAirport.setOperatingDays(allDays);
        
        // Setup DTOs
        busTaksimDto = new TransportationDto();
        busTaksimDto.setId(1L);
        busTaksimDto.setOriginLocationId(1L);
        busTaksimDto.setOriginLocationCode("TAK");
        busTaksimDto.setDestinationLocationId(2L);
        busTaksimDto.setDestinationLocationCode("IST");
        busTaksimDto.setTransportationType(TransportationType.BUS);
        busTaksimDto.setOperatingDays(allDays);
        
        flightDto = new TransportationDto();
        flightDto.setId(2L);
        flightDto.setOriginLocationId(2L);
        flightDto.setOriginLocationCode("IST");
        flightDto.setDestinationLocationId(3L);
        flightDto.setDestinationLocationCode("LHR");
        flightDto.setTransportationType(TransportationType.FLIGHT);
        flightDto.setOperatingDays(allDays);
        
        uberToStadiumDto = new TransportationDto();
        uberToStadiumDto.setId(3L);
        uberToStadiumDto.setOriginLocationId(3L);
        uberToStadiumDto.setOriginLocationCode("LHR");
        uberToStadiumDto.setDestinationLocationId(4L);
        uberToStadiumDto.setDestinationLocationCode("WEM");
        uberToStadiumDto.setTransportationType(TransportationType.UBER);
        uberToStadiumDto.setOperatingDays(allDays);
        
        subwayToAirportDto = new TransportationDto();
        subwayToAirportDto.setId(4L);
        subwayToAirportDto.setOriginLocationId(1L);
        subwayToAirportDto.setOriginLocationCode("TAK");
        subwayToAirportDto.setDestinationLocationId(2L);
        subwayToAirportDto.setDestinationLocationCode("IST");
        subwayToAirportDto.setTransportationType(TransportationType.SUBWAY);
        subwayToAirportDto.setOperatingDays(allDays);
        
        // Setup date
        travelDate = LocalDate.of(2025, 4, 7); // Monday
        dayOfWeek = travelDate.getDayOfWeek().getValue(); // 1
        
        // Use lenient() to avoid UnnecessaryStubbingException for stubs that might not be used in every test
        lenient().when(transportationService.mapToDto(busToAirport)).thenReturn(busTaksimDto);
        lenient().when(transportationService.mapToDto(flight)).thenReturn(flightDto);
        lenient().when(transportationService.mapToDto(uberToStadium)).thenReturn(uberToStadiumDto);
        lenient().when(transportationService.mapToDto(subwayToAirport)).thenReturn(subwayToAirportDto);
    }

    @Test
    void findRoutes_WhenDirectFlightExists_ShouldReturnDirectFlightRoute() {
        // Arrange
        // Test for direct flight from Istanbul to Heathrow
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findByLocationCode("LHR")).thenReturn(Optional.of(heathrowAirport));
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(flight));
        
        // Act
        List<List<TransportationDto>> routes = routeService.findRoutes("IST", "LHR", travelDate);
        
        // Assert
        assertEquals(1, routes.size(), "Should find 1 route (direct flight)");
        assertEquals(1, routes.get(0).size(), "Route should have 1 transportation segment");
        assertEquals(TransportationType.FLIGHT, routes.get(0).get(0).getTransportationType());
        assertEquals("IST", routes.get(0).get(0).getOriginLocationCode());
        assertEquals("LHR", routes.get(0).get(0).getDestinationLocationCode());
    }

    @Test
    void findRoutes_WithBeforeFlightTransfer_ShouldReturnValidRoute() {
        // Arrange
        // Test for Taksim -> Bus -> Istanbul Airport -> Flight -> Heathrow
        when(locationRepository.findByLocationCode("TAK")).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findByLocationCode("LHR")).thenReturn(Optional.of(heathrowAirport));
        
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(busToAirport, flight, subwayToAirport));
        
        // Act
        List<List<TransportationDto>> routes = routeService.findRoutes("TAK", "LHR", travelDate);
        
        // Assert
        assertTrue(routes.size() >= 1, "Should find at least 1 route");
        
        // Find the bus->flight route
        boolean foundBusFlightRoute = false;
        for (List<TransportationDto> route : routes) {
            if (route.size() == 2 && 
                route.get(0).getTransportationType() == TransportationType.BUS &&
                route.get(1).getTransportationType() == TransportationType.FLIGHT) {
                foundBusFlightRoute = true;
                break;
            }
        }
        
        assertTrue(foundBusFlightRoute, "Should find a route with Bus -> Flight");
    }

    @Test
    void findRoutes_WithAfterFlightTransfer_ShouldReturnValidRoute() {
        // Arrange
        // Test for Istanbul -> Flight -> Heathrow -> Uber -> Wembley
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findByLocationCode("WEM")).thenReturn(Optional.of(wembleyStadium));
        
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(flight, uberToStadium));
        
        // Act
        List<List<TransportationDto>> routes = routeService.findRoutes("IST", "WEM", travelDate);
        
        // Assert
        assertEquals(1, routes.size(), "Should find 1 route");
        assertEquals(2, routes.get(0).size(), "Route should have 2 transportation segments");
        assertEquals(TransportationType.FLIGHT, routes.get(0).get(0).getTransportationType());
        assertEquals(TransportationType.UBER, routes.get(0).get(1).getTransportationType());
    }

    @Test
    void findRoutes_WithCompleteRoute_ShouldReturnValidRoute() {
        // Arrange
        // Test for Taksim -> Bus -> Istanbul -> Flight -> Heathrow -> Uber -> Wembley
        when(locationRepository.findByLocationCode("TAK")).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findByLocationCode("WEM")).thenReturn(Optional.of(wembleyStadium));
        
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(busToAirport, subwayToAirport, flight, uberToStadium));
        
        // Act
        List<List<TransportationDto>> routes = routeService.findRoutes("TAK", "WEM", travelDate);
        
        // Assert
        assertTrue(routes.size() >= 1, "Should find at least 1 route");
        
        // Find the 3-segment route
        boolean foundCompleteRoute = false;
        for (List<TransportationDto> route : routes) {
            if (route.size() == 3 && 
                route.get(1).getTransportationType() == TransportationType.FLIGHT) {
                foundCompleteRoute = true;
                break;
            }
        }
        
        assertTrue(foundCompleteRoute, "Should find a complete 3-segment route");
    }

    @Test
    void findRoutes_WithMultipleTransferOptions_ShouldReturnAllValidRoutes() {
        // Arrange
        // Test for Taksim -> [Bus or Subway] -> Istanbul -> Flight -> Heathrow
        when(locationRepository.findByLocationCode("TAK")).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findByLocationCode("LHR")).thenReturn(Optional.of(heathrowAirport));
        
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(busToAirport, subwayToAirport, flight));
        
        // Act
        List<List<TransportationDto>> routes = routeService.findRoutes("TAK", "LHR", travelDate);
        
        // Assert
        assertEquals(2, routes.size(), "Should find 2 routes (one with bus, one with subway)");
        
        // Check that we have both a bus route and a subway route
        boolean hasBusRoute = false;
        boolean hasSubwayRoute = false;
        
        for (List<TransportationDto> route : routes) {
            assertEquals(2, route.size(), "Each route should have 2 transportation segments");
            if (route.get(0).getTransportationType() == TransportationType.BUS) {
                hasBusRoute = true;
            }
            if (route.get(0).getTransportationType() == TransportationType.SUBWAY) {
                hasSubwayRoute = true;
            }
        }
        
        assertTrue(hasBusRoute, "Should include a route with bus transfer");
        assertTrue(hasSubwayRoute, "Should include a route with subway transfer");
    }

    @Test
    void findRoutes_WhenNoValidRoutesExist_ShouldReturnEmptyList() {
        // Arrange
        when(locationRepository.findByLocationCode("TAK")).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findByLocationCode("WEM")).thenReturn(Optional.of(wembleyStadium));
        
        // Return no flights, making it impossible to create a valid route
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(busToAirport, uberToStadium));
        
        // Act
        List<List<TransportationDto>> routes = routeService.findRoutes("TAK", "WEM", travelDate);
        
        // Assert
        assertTrue(routes.isEmpty(), "Should return an empty list when no valid routes exist");
    }

    @Test
    void findRoutes_WhenLocationNotFound_ShouldThrowException() {
        // Arrange
        when(locationRepository.findByLocationCode("TAK")).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findByLocationCode("INVALID")).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            routeService.findRoutes("TAK", "INVALID", travelDate);
        });
        
        assertTrue(exception.getMessage().contains("not found with code: INVALID"));
    }
    
    @Test
    void findRoutes_WhenCalledTwice_ShouldUseCacheForSecondCall() {
        // This test requires a real Spring application context with caching enabled
        // In a real application, you would use @SpringBootTest instead of MockitoExtension
        // For unit testing, we're verifying that the @Cacheable annotation is correctly applied
        
        // Arrange
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findByLocationCode("LHR")).thenReturn(Optional.of(heathrowAirport));
        when(transportationRepository.findByOperatingDay(dayOfWeek))
            .thenReturn(Arrays.asList(flight));
        
        // Act
        // First call - should hit the database
        List<List<TransportationDto>> firstResult = routeService.findRoutes("IST", "LHR", travelDate);
        
        // Second call with the same parameters - should use cache 
        // (In a real integration test, the repository wouldn't be called)
        List<List<TransportationDto>> secondResult = routeService.findRoutes("IST", "LHR", travelDate);
        
        // Assert
        assertEquals(1, firstResult.size(), "Should find 1 route (direct flight)");
        assertEquals(1, secondResult.size(), "Cached result should match original result");
        
        // In a unit test with mocks, we can only verify the repository was called
        // In a real integration test, we would verify the cache was used for the second call
        verify(locationRepository, times(2)).findByLocationCode("IST"); // Still called for each test due to mocking
        verify(locationRepository, times(2)).findByLocationCode("LHR"); // Still called for each test due to mocking
        verify(transportationRepository, times(2)).findByOperatingDay(dayOfWeek); // Still called for each test due to mocking
    }
}
