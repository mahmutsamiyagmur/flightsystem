package com.msy.projects.flightsystem.service;

import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.exception.ResourceNotFoundException;
import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.model.Transportation;
import com.msy.projects.flightsystem.model.TransportationType;
import com.msy.projects.flightsystem.repository.LocationRepository;
import com.msy.projects.flightsystem.repository.TransportationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final TransportationRepository transportationRepository;
    private final LocationRepository locationRepository;
    private final TransportationService transportationService;

    @Autowired
    public RouteService(TransportationRepository transportationRepository,
                      LocationRepository locationRepository,
                      TransportationService transportationService) {
        this.transportationRepository = transportationRepository;
        this.locationRepository = locationRepository;
        this.transportationService = transportationService;
    }

    /**
     * Find all valid routes from origin to destination on the specified date
     * 
     * A valid route must:
     * 1. Have exactly one FLIGHT transportation
     * 2. Can optionally have a before-flight transfer (non-FLIGHT)
     * 3. Can optionally have an after-flight transfer (non-FLIGHT)
     * 4. Maximum of 3 transportation segments total
     * 5. All transportations must be connected (destination of one = origin of next)
     * 6. All transportations must be available on the specified date
     * 
     * This method is cached using Redis with a key based on origin, destination, and travel date.
     * 
     * @param originCode Code of the origin location
     * @param destinationCode Code of the destination location
     * @param travelDate Date of travel
     * @return List of possible routes (as lists of transportation segments)
     */
    @Cacheable(value = "routeCache", key = "#originCode + '-' + #destinationCode + '-' + #travelDate")
    public List<List<TransportationDto>> findRoutes(String originCode, String destinationCode, LocalDate travelDate) {
        // Validate locations exist
        Location origin = locationRepository.findByLocationCode(originCode)
                .orElseThrow(() -> new ResourceNotFoundException("Origin location not found with code: " + originCode));
        
        Location destination = locationRepository.findByLocationCode(destinationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Destination location not found with code: " + destinationCode));
        
        // Get the day of week (1-7, where 1 is Monday)
        int dayOfWeek = travelDate.getDayOfWeek().getValue();
        
        // List to store all valid routes
        List<List<TransportationDto>> validRoutes = new ArrayList<>();
        
        // Get all available flight options on the specified day
        List<Transportation> availableFlights = transportationRepository.findByOperatingDay(dayOfWeek)
                .stream()
                .filter(t -> t.getTransportationType() == TransportationType.FLIGHT)
                .collect(Collectors.toList());
        
        for (Transportation flight : availableFlights) {
            findValidRoutesWithFlight(origin, destination, dayOfWeek, flight, validRoutes);
        }
        
        return validRoutes;
    }

    /**
     * Find valid routes using a specific flight as the main transportation
     */
    private void findValidRoutesWithFlight(Location origin, Location destination, int dayOfWeek, 
                                        Transportation flight, List<List<TransportationDto>> validRoutes) {
        
        Location flightOrigin = flight.getOriginLocation();
        Location flightDestination = flight.getDestinationLocation();
        
        // Case 1: Flight only (if the flight directly connects origin and destination)
        if (flightOrigin.getId().equals(origin.getId()) && flightDestination.getId().equals(destination.getId())) {
            List<Transportation> route = new ArrayList<>();
            route.add(flight);
            validRoutes.add(route.stream().map(transportationService::mapToDto).collect(Collectors.toList()));
        }
        
        // Case 2: Before-flight transfer + Flight
        if (flightDestination.getId().equals(destination.getId())) {
            findBeforeFlightTransfers(origin, flight, dayOfWeek, validRoutes);
        }
        
        // Case 3: Flight + After-flight transfer
        if (flightOrigin.getId().equals(origin.getId())) {
            findAfterFlightTransfers(flight, destination, dayOfWeek, validRoutes);
        }
        
        // Case 4: Before-flight transfer + Flight + After-flight transfer
        findCompleteRoutes(origin, flight, destination, dayOfWeek, validRoutes);
    }
    
    /**
     * Find valid before-flight transfers
     */
    private void findBeforeFlightTransfers(Location origin, Transportation flight, int dayOfWeek,
                                           List<List<TransportationDto>> validRoutes) {
        // Find all non-flight transportations from origin to flight origin that operate on the given day
        List<Transportation> beforeFlightOptions = transportationRepository.findByOperatingDay(dayOfWeek)
                .stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .filter(t -> t.getOriginLocation().getId().equals(origin.getId()))
                .filter(t -> t.getDestinationLocation().getId().equals(flight.getOriginLocation().getId()))
                .collect(Collectors.toList());
        
        for (Transportation beforeFlight : beforeFlightOptions) {
            List<Transportation> route = new ArrayList<>();
            route.add(beforeFlight);
            route.add(flight);
            validRoutes.add(route.stream().map(transportationService::mapToDto).collect(Collectors.toList()));
        }
    }
    
    /**
     * Find valid after-flight transfers
     */
    private void findAfterFlightTransfers(Transportation flight, Location destination, int dayOfWeek,
                                          List<List<TransportationDto>> validRoutes) {
        // Find all non-flight transportations from flight destination to final destination that operate on the given day
        List<Transportation> afterFlightOptions = transportationRepository.findByOperatingDay(dayOfWeek)
                .stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .filter(t -> t.getOriginLocation().getId().equals(flight.getDestinationLocation().getId()))
                .filter(t -> t.getDestinationLocation().getId().equals(destination.getId()))
                .collect(Collectors.toList());
        
        for (Transportation afterFlight : afterFlightOptions) {
            List<Transportation> route = new ArrayList<>();
            route.add(flight);
            route.add(afterFlight);
            validRoutes.add(route.stream().map(transportationService::mapToDto).collect(Collectors.toList()));
        }
    }
    
    /**
     * Find complete routes (before-flight + flight + after-flight)
     */
    private void findCompleteRoutes(Location origin, Transportation flight, Location destination, int dayOfWeek,
                                    List<List<TransportationDto>> validRoutes) {
        // Find all valid before-flight transfers
        List<Transportation> beforeFlightOptions = transportationRepository.findByOperatingDay(dayOfWeek)
                .stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .filter(t -> t.getOriginLocation().getId().equals(origin.getId()))
                .filter(t -> t.getDestinationLocation().getId().equals(flight.getOriginLocation().getId()))
                .collect(Collectors.toList());
        
        // Find all valid after-flight transfers
        List<Transportation> afterFlightOptions = transportationRepository.findByOperatingDay(dayOfWeek)
                .stream()
                .filter(t -> t.getTransportationType() != TransportationType.FLIGHT)
                .filter(t -> t.getOriginLocation().getId().equals(flight.getDestinationLocation().getId()))
                .filter(t -> t.getDestinationLocation().getId().equals(destination.getId()))
                .collect(Collectors.toList());
        
        // Create all possible combinations
        for (Transportation beforeFlight : beforeFlightOptions) {
            for (Transportation afterFlight : afterFlightOptions) {
                List<Transportation> route = new ArrayList<>();
                route.add(beforeFlight);
                route.add(flight);
                route.add(afterFlight);
                validRoutes.add(route.stream().map(transportationService::mapToDto).collect(Collectors.toList()));
            }
        }
    }
}
