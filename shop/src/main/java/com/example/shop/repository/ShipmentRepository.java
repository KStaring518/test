package com.example.shop.repository;

import com.example.shop.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    Optional<Shipment> findByOrderId(Long orderId);
    
    Optional<Shipment> findByShipmentNo(String shipmentNo);
    
    List<Shipment> findByStatus(Shipment.ShipmentStatus status);
}


