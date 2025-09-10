package com.example.shop.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.shop.config.AlipayConfig;
import com.example.shop.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayClient alipayClient;

    /** 创建支付宝电脑网站支付页面(表单HTML) */
    public String createPaymentPage(Order order) throws Exception {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        String bizContent = "{" +
                "\"out_trade_no\":\"" + order.getOrderNo() + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "\"total_amount\":\"" + order.getTotalAmount().toPlainString() + "\"," +
                "\"subject\":\"零食商城订单-" + order.getOrderNo() + "\"" +
                "}";
        request.setBizContent(bizContent);
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            System.out.println("[Alipay][Create] success for order " + order.getOrderNo() + ", amount=" + order.getTotalAmount());
            return response.getBody();
        } else {
            throw new RuntimeException("创建支付页面失败: " + response.getSubMsg());
        }
    }

    /** 验证支付宝异步通知签名 */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
            );
        } catch (Exception e) {
            return false;
        }
    }

    /** 处理支付宝异步通知（表单参数） */
    public String handleNotify(Map<String, String[]> requestParams) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i]);
                if (i < values.length - 1) sb.append(",");
            }
            params.put(name, sb.toString());
        }

        boolean signOk = verifyNotify(params);
        System.out.println("[Alipay][Notify] sign=" + signOk);
        if (!signOk) {
            return "fail";
        }

        String appId = params.getOrDefault("app_id", "");
        String outTradeNo = params.getOrDefault("out_trade_no", "");
        String tradeNo = params.getOrDefault("trade_no", "");
        String tradeStatus = params.getOrDefault("trade_status", "");
        String totalAmount = params.getOrDefault("total_amount", "0");

        if (!alipayConfig.getAppId().equals(appId)) {
            System.out.println("[Alipay][Notify] appId mismatch: got=" + appId + ", cfg=" + alipayConfig.getAppId());
            return "fail";
        }
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            try {
                Order order = orderService.getOrderByOrderNo(outTradeNo);
                if (order == null) {
                    System.out.println("[Alipay][Notify] order not found: " + outTradeNo);
                    return "success";
                }
                if (order.getTotalAmount() != null && !order.getTotalAmount().toPlainString().equals(totalAmount)) {
                    System.out.println("[Alipay][Notify] amount mismatch: notify=" + totalAmount + ", db=" + order.getTotalAmount());
                    return "fail";
                }
                orderService.markPaidByGateway(outTradeNo, tradeNo);
                System.out.println("[Alipay][Notify] mark paid ok: orderNo=" + outTradeNo + ", tradeNo=" + tradeNo);
                return "success";
            } catch (Exception e) {
                System.out.println("[Alipay][Notify] exception while marking paid: " + e.getMessage());
                return "fail";
            }
        }
        return "success";
    }
}
