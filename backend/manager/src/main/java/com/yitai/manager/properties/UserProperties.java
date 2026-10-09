package com.yitai.manager.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "yitai.auth")
public class UserProperties {

    private List<String> noAuthUrls;
}
