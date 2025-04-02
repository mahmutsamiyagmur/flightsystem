package com.msy.projects.flightsystem.service;

import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.model.Transportation;
import com.msy.projects.flightsystem.repository.LocationRepository;
import com.msy.projects.flightsystem.repository.TransportationRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @param originCode Code of the origin location
     * @param destinationCode Code of the destination location
     * @param travelDate Date of travel
     * @return List of possible routes (as lists of transportation segments)
     */
    public List<List<TransportationDto>> findRoutes(String originCode, String destinationCode, LocalDate travelDate) {
        // Validate locations exist
        Location origin = locationRepository.findByLocationCode(originCode)
                .orElseThrow(() -> new RuntimeException("Origin location not found with code: " + originCode));
        
        Location destination = locationRepository.findByLocationCode(destinationCode)
                .orElseThrow(() -> new RuntimeException("Destination location not found with code: " + destinationCode));
        
        // Get the day of week (1-7, where 1 is Monday)
        int dayOfWeek = travelDate.getDayOfWeek().getValue();
        
        // List to store all valid routes
        List<List<TransportationDto>> routes = new ArrayList<>();
        
        // Use a breadth-first search approach to find all possible routes
        findAllRoutes(origin, destination, dayOfWeek, new ArrayList<>(), new HashSet<>(), routes);
        
        return routes;
    }

    private void findAllRoutes(Location current, Location destination, int dayOfWeek, 
                            List<Transportation> currentRoute, Set<Long> visitedLocations,
                            List<List<TransportationDto>> validRoutes) {
        
        // Avoid cycles by tracking visited locations
        if (visitedLocations.contains(current.getId())) {
            return;
        }
        
        // If we've reached a reasonable limit of transfers (4 is a reasonable number)
        if (currentRoute.size() >= 4) {
            return;
        }
        
        // Mark current location as visited to avoid cycles
        visitedLocations.add(current.getId());
        
        // Get all transportation options from the current location that operate on the given day
        List<Transportation> options = transportationRepository.findByOriginLocationAndOperatingDay(current, dayOfWeek);
        
        for (Transportation transport : options) {
            // Add this transportation to our current route
            List<Transportation> newRoute = new ArrayList<>(currentRoute);
            newRoute.add(transport);
            
            Location nextStop = transport.getDestinationLocation();
            
            // If we've reached the destination, add this route to our valid routes
            if (nextStop.getId().equals(destination.getId())) {
                validRoutes.add(newRoute.stream()
                                .map(transportationService::mapToDto)
                                .collect(Collectors.toList()));
            } else {
                // Continue searching for routes from the next location
                Set<Long> newVisited = new HashSet<>(visitedLocations);
                findAllRoutes(nextStop, destination, dayOfWeek, newRoute, newVisited, validRoutes);
            }
        }
    }
}
