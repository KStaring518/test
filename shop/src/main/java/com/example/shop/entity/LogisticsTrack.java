package com.example.shop.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "logistics_tracks")
public class LogisticsTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String description;

    @Column(name = "track_time")
    private LocalDateTime trackTime;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


