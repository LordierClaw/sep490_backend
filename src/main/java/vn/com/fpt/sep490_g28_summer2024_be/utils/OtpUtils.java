package vn.com.fpt.sep490_g28_summer2024_be.utils;

import com.google.common.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.fpt.sep490_g28_summer2024_be.common.AppConfig;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;


@Builder
@Component
public class OtpUtils {

    private Cache<String, String> otpCache;

    public String generateOtp(){
        Random random = new Random();
        int otp = random.nextInt(999999-100000) +100000;
        return String.valueOf(otp);
    }

    public String get(String key){
        return otpCache.getIfPresent(key);
    }

    public void add(String key, String value){
        otpCache.put(key, value);
    }

    public void delete(String key){
        otpCache.invalidate(key);
    }

}
