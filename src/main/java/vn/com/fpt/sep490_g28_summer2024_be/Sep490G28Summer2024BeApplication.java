package vn.com.fpt.sep490_g28_summer2024_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Sep490G28Summer2024BeApplication{
    public static void main(String[] args) {
        SpringApplication.run(Sep490G28Summer2024BeApplication.class, args);
    }

}