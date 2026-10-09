package com.yitai.common.interceptor;

import com.alibaba.fastjson2.JSON;
import com.yitai.common.exception.YitaiException;
import com.yitai.model.entity.user.UserInfo;
import com.yitai.model.vo.common.ResultCodeEnum;
import com.yitai.utils.AuthContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;

@Slf4j
public class UserLoginAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("========== 拦截器开始处理请求: {}", requestURI);
        log.info("请求方法: {}", request.getMethod());
        log.info("所有请求头: {}", Collections.list(request.getHeaderNames()));
        
        // 优先从网关传递的 X-User-Id 获取用户信息
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-Username");
        
        if (StringUtils.hasText(userIdHeader) && StringUtils.hasText(usernameHeader)) {
            // 网关已经验证过，从token获取完整用户信息
            log.info("检测到网关用户信息，用户ID: {}, 用户名: {}", userIdHeader, usernameHeader);
            
            // 从请求头获取token
            String token = request.getHeader("token");
            if (StringUtils.hasText(token)) {
                // 从redis获取完整用户信息
                String redisKey = "user:yitai:" + token;
                String userJson = redisTemplate.opsForValue().get(redisKey);
                
                if (StringUtils.hasText(userJson)) {
                    UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
                    log.info("从Redis获取完整用户信息，昵称: {}, 头像: {}", userInfo.getNickName(), userInfo.getAvatar());
                    AuthContextUtil.setUserInfo(userInfo);
                    log.info("========== 拦截器处理完成（使用Redis完整用户信息），放行请求\n");
                    return true;
                }
            }
            
            // 如果Redis中没有，则使用网关传递的基本信息
            log.warn("Redis中未找到用户信息，使用网关传递的基本信息");
            UserInfo userInfo = new UserInfo();
            userInfo.setId(Long.parseLong(userIdHeader));
            userInfo.setUsername(usernameHeader);
            AuthContextUtil.setUserInfo(userInfo);
            
            log.info("========== 拦截器处理完成（使用网关基本信息），放行请求\n");
            return true;
        }
        
        // 如果没有网关传递的用户信息，则使用 token 从 Redis 获取
        log.warn("未找到网关传递的用户信息，尝试从 token 获取");
        
        // 从请求头获取token
        String token = request.getHeader("token");
        log.info("请求头中的 token: {}", token);
        
        // 判断token是否为空
        if (!StringUtils.hasText(token)) {
            // 判断是否为鉴权路径
            if (antPathMatcher.match("/api/**/auth/**", requestURI)) {
                log.error("鉴权路径token为空，拒绝访问");
                throw new YitaiException(ResultCodeEnum.LOGIN_AUTH);
            }
            log.warn("token为空，当前请求非鉴权路径，静默放行");
            return true;  // 非鉴权路径，不强制登录
        }
        
        // 构建Redis key
        String redisKey = "user:yitai:" + token;
        log.info("Redis key: {}", redisKey);
        
        // 从redis获取用户信息
        String userJson = redisTemplate.opsForValue().get(redisKey);
        log.info("从Redis获取的用户信息: {}", userJson);
        
        // 判断用户信息是否存在
        if (!StringUtils.hasText(userJson)) {
            // 判断是否为鉴权路径
            if (antPathMatcher.match("/api/**/auth/**", requestURI)) {
                log.error("鉴权路径Redis中未找到用户信息，key: {}", redisKey);
                throw new YitaiException(ResultCodeEnum.LOGIN_AUTH);
            }
            log.warn("Redis中未找到用户信息，key: {}，静默放行", redisKey);
            return true;  // 用户信息过期，非鉴权路径，静默放行
        }
        
        // 放到threadLocal里面
        UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
        log.info("成功解析用户信息，用户ID: {}, 用户名: {}", userInfo.getId(), userInfo.getUsername());
        AuthContextUtil.setUserInfo(userInfo);
        
        log.info("========== 拦截器处理完成（使用Redis用户信息），放行请求\n");
        return true;
    }
}
