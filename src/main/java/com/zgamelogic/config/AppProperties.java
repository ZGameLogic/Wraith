package com.zgamelogic.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {
    @Getter
    private static String secretKey;

    @Value("${app.secret-key}")
    public void setSecretKey(String key) {
        AppProperties.secretKey = key;
    }
}
