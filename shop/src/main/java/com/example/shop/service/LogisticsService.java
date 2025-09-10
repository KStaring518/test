package com.example.shop.service;

import com.example.shop.common.BusinessException;
import com.example.shop.entity.LogisticsTrack;
import com.example.shop.entity.Shipment;
import com.example.shop.repository.LogisticsTrackRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ShipmentRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
 

@Service
public class LogisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private LogisticsTrackRepository logisticsTrackRepository;

    /**
     * 获取订单物流信息
     */
    public Shipment getOrderLogistics(String username, Long orderId) {
        userRepository.findByUsername(username).orElseThrow(() -> new BusinessException("用户不存在"));
        if (!orderRepository.existsById(orderId)) {
            throw new BusinessException("订单不存在");
        }
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("物流信息不存在"));
    }

    /**
     * 获取物流轨迹
     */
    public List<LogisticsTrack> getLogisticsTracks(Long shipmentId) {
        return logisticsTrackRepository.findByShipmentIdOrderByTrackTimeAsc(shipmentId);
    }

    /**
     * 模拟物流更新（定时任务）
     * 调整为每 1 分钟推进一阶段，去除随机性，便于可视化演示。
     */
    @Scheduled(fixedRate = 120000) // 每2分钟执行一次
    @Transactional
    public void simulateLogisticsUpdate() {
        // 先推进“运输中”到“已送达”，避免同一轮内从已发货直接推进两次导致时间线乱序
        List<Shipment> inTransitList = shipmentRepository.findByStatus(Shipment.ShipmentStatus.IN_TRANSIT);
        for (Shipment shipment : inTransitList) {
            advanceInTransitToDelivered(shipment);
        }

        // 再把“已发货”推进到“运输中”
        List<Shipment> shippedList = shipmentRepository.findByStatus(Shipment.ShipmentStatus.SHIPPED);
        for (Shipment shipment : shippedList) {
            advanceShippedToInTransit(shipment);
        }

        // 已送达超过10分钟，自动签收
        List<Shipment> deliveredList = shipmentRepository.findByStatus(Shipment.ShipmentStatus.DELIVERED);
        LocalDateTime now = LocalDateTime.now();
        for (Shipment shipment : deliveredList) {
            if (shipment.getActualDelivery() != null && shipment.getActualDelivery().isBefore(now.minusMinutes(10))) {
                shipment.setStatus(Shipment.ShipmentStatus.RECEIVED);
                addLogisticsTrackWithTime(shipment, "已签收", "系统已自动签收", now);
                // 自动完成订单
                if (shipment.getOrder() != null) {
                    shipment.getOrder().setStatus(com.example.shop.entity.Order.OrderStatus.FINISHED);
                    orderRepository.save(shipment.getOrder());
                }
                shipmentRepository.save(shipment);
            }
        }
    }

    /**
     * 更新发货单状态
     */
    private void advanceShippedToInTransit(Shipment shipment) {
        shipment.setStatus(Shipment.ShipmentStatus.IN_TRANSIT);
        // 记录到达物流中心
        addLogisticsTrack(shipment, "物流中心", "包裹已到达物流中心，正在分拣");
        shipmentRepository.save(shipment);
    }

    private void advanceInTransitToDelivered(Shipment shipment) {
        LocalDateTime base = LocalDateTime.now();
        shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
        shipment.setActualDelivery(base.plusSeconds(20));
        // 严格递增时间：配送点 -> 配送中 -> 已送达
        addLogisticsTrackWithTime(shipment, "配送点", "包裹已到达配送点，等待配送员取件", base);
        addLogisticsTrackWithTime(shipment, "配送中", "配送员正在配送中", base.plusSeconds(10));
        addLogisticsTrackWithTime(shipment, "已送达", "包裹已送达，请及时签收", base.plusSeconds(20));
        shipmentRepository.save(shipment);
    }

    /**
     * 添加物流轨迹
     */
    private void addLogisticsTrack(Shipment shipment, String location, String description) {
        LogisticsTrack track = new LogisticsTrack();
        track.setShipment(shipment);
        track.setLocation(location);
        track.setDescription(description);
        track.setTrackTime(LocalDateTime.now());
        logisticsTrackRepository.save(track);
    }

    private void addLogisticsTrackWithTime(Shipment shipment, String location, String description, LocalDateTime time) {
        LogisticsTrack track = new LogisticsTrack();
        track.setShipment(shipment);
        track.setLocation(location);
        track.setDescription(description);
        track.setTrackTime(time);
        logisticsTrackRepository.save(track);
    }
}
