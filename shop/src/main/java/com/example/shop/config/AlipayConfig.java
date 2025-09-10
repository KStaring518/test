package com.example.shop.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.merchant-private-key}")
    private String merchantPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.charset}")
    private String charset;

    @Value("${alipay.format}")
    private String format;

    @Value("${alipay.sign-type}")
    private String signType;

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
            gatewayUrl,
            appId,
            merchantPrivateKey,
            format,
            charset,
            alipayPublicKey,
            signType
        );
    }

    public String getNotifyUrl() { return notifyUrl; }
    public String getReturnUrl() { return returnUrl; }

    public String getAppId() { return appId; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public String getCharset() { return charset; }
    public String getSignType() { return signType; }
}
