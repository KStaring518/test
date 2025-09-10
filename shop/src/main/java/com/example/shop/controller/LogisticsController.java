package com.example.shop.controller;

import com.example.shop.common.Result;
import com.example.shop.entity.LogisticsTrack;
import com.example.shop.entity.Shipment;
import com.example.shop.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logistics")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    /**
     * 获取订单物流信息
     */
    @GetMapping("/order/{orderId}")
    public Result<Shipment> getOrderLogistics(Authentication auth, @PathVariable Long orderId) {
        return Result.success(logisticsService.getOrderLogistics(auth.getName(), orderId));
    }

    /**
     * 获取物流轨迹
     */
    @GetMapping("/shipment/{shipmentId}/tracks")
    public Result<List<LogisticsTrack>> getLogisticsTracks(@PathVariable Long shipmentId) {
        return Result.success(logisticsService.getLogisticsTracks(shipmentId));
    }

    /**
     * 模拟物流更新（定时任务调用）
     */
    @PostMapping("/simulate-update")
    public Result<Void> simulateLogisticsUpdate() {
        logisticsService.simulateLogisticsUpdate();
        return Result.success();
    }
}
