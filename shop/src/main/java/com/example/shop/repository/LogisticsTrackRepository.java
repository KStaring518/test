package com.example.shop.repository;

import com.example.shop.entity.LogisticsTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogisticsTrackRepository extends JpaRepository<LogisticsTrack, Long> {
    
    @Query("SELECT lt FROM LogisticsTrack lt WHERE lt.shipment.id = :shipmentId ORDER BY lt.trackTime ASC")
    List<LogisticsTrack> findByShipmentIdOrderByTrackTimeAsc(@Param("shipmentId") Long shipmentId);

    // 获取当前发货单最新一条轨迹（用于保证时间单调递增）
    LogisticsTrack findTopByShipment_IdOrderByTrackTimeDesc(Long shipmentId);
}


