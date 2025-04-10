package vn.com.fpt.sep490_g28_summer2024_be.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;

import java.util.concurrent.TimeUnit;


@Configuration
public class CacheConfig {
    @Bean
    public Cache<String, String> cacheOtp(){
        return CacheBuilder.newBuilder()
                .expireAfterWrite(AppConfig.VALID_OTP_TIME, TimeUnit.SECONDS)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }
}
