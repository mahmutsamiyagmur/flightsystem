package com.msy.projects.flightsystem.repository;

import com.msy.projects.flightsystem.model.Location;
import com.msy.projects.flightsystem.model.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportationRepository extends JpaRepository<Transportation, Long> {
    List<Transportation> findByOriginLocation(Location originLocation);
    
    List<Transportation> findByOriginLocationAndDestinationLocation(
            Location originLocation, Location destinationLocation);
    
    @Query("SELECT t FROM Transportation t WHERE :dayOfWeek MEMBER OF t.operatingDays")
    List<Transportation> findByOperatingDay(@Param("dayOfWeek") Integer dayOfWeek);
    
    @Query("SELECT t FROM Transportation t WHERE :dayOfWeek MEMBER OF t.operatingDays " +
           "AND t.originLocation = :originLocation")
    List<Transportation> findByOriginLocationAndOperatingDay(
            @Param("originLocation") Location originLocation, 
            @Param("dayOfWeek") Integer dayOfWeek);
}
