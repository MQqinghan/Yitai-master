package com.yitai.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.yitai.model.entity.user.UserInfo;
import com.yitai.model.vo.common.Result;
import com.yitai.model.vo.common.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate<String , String> redisTemplate;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求路径
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 尝试获取用户信息（无论路径是否需要鉴权）
        UserInfo userInfo = this.getUserInfo(request);
        
        // 2. 如果获取到用户信息，放入请求头传递给下游服务
        if (userInfo != null) {
            request = request.mutate()
                    .header("X-User-Id", String.valueOf(userInfo.getId()))
                    .header("X-Username", userInfo.getUsername())
                    .build();
            // 重新设置请求到exchange
            exchange = exchange.mutate().request(request).build();
        }

        // 3. 判断路径 /api/**/auth/**，如果是鉴权路径但用户未登录，则拦截
        if(antPathMatcher.match("/api/**/auth/**",path)) {
            if(userInfo == null) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //放行
        return chain.filter(exchange);
    }


    private UserInfo getUserInfo(ServerHttpRequest request) {
        //从请求头获取token
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(tokenList != null) {
            token = tokenList.get(0);
        }
        
        System.out.println("[网关调试] 请求路径: " + request.getURI().getPath());
        System.out.println("[网关调试] 获取到的 token: " + token);
        
        //判断token是否为空
        if(!StringUtils.isEmpty(token)) {
            //根据token查询redis
            String redisKey = "user:yitai:" + token;
            System.out.println("[网关调试] Redis key: " + redisKey);
            String userJson = redisTemplate.opsForValue().get(redisKey);
            System.out.println("[网关调试] Redis 返回的用户信息: " + userJson);
            
            if(StringUtils.isEmpty(userJson)) {
                return null;
            } else {
                UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
                return userInfo;
            }
        }
        System.out.println("[网关调试] token 为空，无法获取用户信息");
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
}
