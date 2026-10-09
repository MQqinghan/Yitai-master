package com.yitai.pay;

import com.yitai.common.anno.EnableUserLoginAuthInterceptor;
import com.yitai.common.anno.EnableUserTokenFeignInterceptor;
import com.yitai.pay.utils.AlipayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.yitai"})
@EnableConfigurationProperties(value = {AlipayProperties.class})
@EnableUserLoginAuthInterceptor
@EnableUserTokenFeignInterceptor
@EnableFeignClients(basePackages = {"com.yitai"})
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class , args) ;
    }

}
