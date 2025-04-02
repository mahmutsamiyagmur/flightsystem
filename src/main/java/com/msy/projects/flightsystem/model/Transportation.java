package com.msy.projects.flightsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "transportations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transportation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "origin_location_id", nullable = false)
    private Location originLocation;
    
    @ManyToOne
    @JoinColumn(name = "destination_location_id", nullable = false)
    private Location destinationLocation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transportation_type", nullable = false)
    private TransportationType transportationType;
    
    @ElementCollection
    @CollectionTable(name = "transportation_operating_days", 
                    joinColumns = @JoinColumn(name = "transportation_id"))
    @Column(name = "day_of_week")
    private List<Integer> operatingDays; // 1=Monday, 2=Tuesday, ..., 7=Sunday
}
