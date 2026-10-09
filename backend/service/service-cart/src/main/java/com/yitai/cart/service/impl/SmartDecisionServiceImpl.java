package com.yitai.cart.service.impl;

import com.alibaba.fastjson2.JSON;
import com.yitai.cart.service.SmartDecisionService;
import com.yitai.feign.product.ProductFeignClient;
import com.yitai.feign.user.UserFeignClient;
import com.yitai.model.entity.h5.CartInfo;
import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.entity.user.MessageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmartDecisionServiceImpl implements SmartDecisionService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    // 库存预警阈值（库存小于等于10件时提醒）
    private static final int STOCK_THRESHOLD = 10;

    // 价格下降阈值（价格下降比例大于等于5%时提醒）
    private static final BigDecimal PRICE_DROP_THRESHOLD = new BigDecimal("0.05");

    // 去重时间窗口（小时）- 同一商品在8小时内只提醒一次
    private static final long DEDUP_HOURS = 8;

    @Override
    public void checkPriceChanges() {
        log.info("开始检查购物车商品价格变化...");
        // 获取所有用户的购物车key
        Set<String> keys = redisTemplate.keys("user:cart:*");
        if (CollectionUtils.isEmpty(keys)) {
            log.info("没有找到购物车数据");
            return;
        }

        for (String cartKey : keys) {
            try {
                // 从key中提取userId
                String userIdStr = cartKey.replace("user:cart:", "");
                Long userId = Long.parseLong(userIdStr);

                // 获取购物车所有商品
                Set<Object> fields = redisTemplate.opsForHash().keys(cartKey);
                if (CollectionUtils.isEmpty(fields)) {
                    continue;
                }

                for (Object field : fields) {
                    String skuIdStr = field.toString();
                    Object cartInfoObj = redisTemplate.opsForHash().get(cartKey, skuIdStr);
                    if (cartInfoObj == null) {
                        continue;
                    }

                    CartInfo cartInfo = JSON.parseObject(cartInfoObj.toString(), CartInfo.class);
                    Long skuId = cartInfo.getSkuId();

                    try {
                        // 获取最新的商品信息
                        ProductSku productSku = productFeignClient.getBySkuId(skuId);
                        if (productSku != null) {
                            // 对比价格变化
                            BigDecimal oldPrice = cartInfo.getCartPrice();
                            BigDecimal newPrice = productSku.getSalePrice();
                            if (oldPrice != null && newPrice != null && oldPrice.compareTo(newPrice) > 0) {
                                // 计算价格下降比例
                                BigDecimal priceDrop = oldPrice.subtract(newPrice);
                                BigDecimal dropRatio = priceDrop.divide(oldPrice, 2, BigDecimal.ROUND_HALF_UP);
                                if (dropRatio.compareTo(PRICE_DROP_THRESHOLD) >= 0) {
                                    // 价格下降超过阈值，发送降价提醒
                                    sendPriceDropMessage(userId, cartInfo, oldPrice, newPrice);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("检查商品{}价格变化失败", skuId, e);
                    }
                }
            } catch (Exception e) {
                log.error("处理购物车{}失败", cartKey, e);
            }
        }
        log.info("商品价格变化检查完成");
    }

    @Override
    public void checkStockChanges() {
        log.info("开始检查购物车商品库存变化...");
        // 获取所有用户的购物车key
        Set<String> keys = redisTemplate.keys("user:cart:*");
        if (CollectionUtils.isEmpty(keys)) {
            log.info("没有找到购物车数据");
            return;
        }

        for (String cartKey : keys) {
            try {
                // 从key中提取userId
                String userIdStr = cartKey.replace("user:cart:", "");
                Long userId = Long.parseLong(userIdStr);

                // 获取购物车所有商品
                Set<Object> fields = redisTemplate.opsForHash().keys(cartKey);
                if (CollectionUtils.isEmpty(fields)) {
                    continue;
                }

                for (Object field : fields) {
                    String skuIdStr = field.toString();
                    Object cartInfoObj = redisTemplate.opsForHash().get(cartKey, skuIdStr);
                    if (cartInfoObj == null) {
                        continue;
                    }

                    CartInfo cartInfo = JSON.parseObject(cartInfoObj.toString(), CartInfo.class);
                    Long skuId = cartInfo.getSkuId();

                    try {
                        // 获取最新的商品信息
                        ProductSku productSku = productFeignClient.getBySkuId(skuId);
                        if (productSku != null) {
                            Integer stockNum = productSku.getStockNum();
                            if (stockNum != null && stockNum <= STOCK_THRESHOLD && stockNum > 0) {
                                // 库存不足，发送库存预警
                                sendStockWarningMessage(userId, cartInfo, stockNum);
                            } else if (stockNum != null && stockNum == 0) {
                                // 商品已售罄，发送售罄提醒
                                sendSoldOutMessage(userId, cartInfo);
                            }
                        }
                    } catch (Exception e) {
                        log.error("检查商品{}库存变化失败", skuId, e);
                    }
                }
            } catch (Exception e) {
                log.error("处理购物车{}失败", cartKey, e);
            }
        }
        log.info("商品库存变化检查完成");
    }

    @Override
    public void executeSmartDecisionCheck() {
        log.info("========== 开始执行智能决策检查 ==========");
        checkPriceChanges();
        checkStockChanges();
        log.info("========== 智能决策检查完成 ==========");
    }

    /**
     * 发送降价提醒消息
     */
    private void sendPriceDropMessage(Long userId, CartInfo cartInfo, BigDecimal oldPrice, BigDecimal newPrice) {
        // 构建去重Key：remind:price:{userId}:{skuId}
        String dedupKey = "remind:price:" + userId + ":" + cartInfo.getSkuId();
        
        // 获取上次提醒时记录的价格
        String lastRemindedPriceStr = redisTemplate.opsForValue().get(dedupKey);
        
        // 判断是否需要发送提醒
        boolean shouldRemind = false;
        
        if (lastRemindedPriceStr == null) {
            // 首次降价提醒
            shouldRemind = true;
            log.info("首次降价提醒：用户{}，商品：{}", userId, cartInfo.getSkuName());
        } else {
            // 已有提醒记录，检查价格是否有新的变动
            try {
                BigDecimal lastRemindedPrice = new BigDecimal(lastRemindedPriceStr);
                // 如果当前价格与上次提醒的价格不同，说明价格又有新变动
                if (newPrice.compareTo(lastRemindedPrice) != 0) {
                    shouldRemind = true;
                    log.info("价格再次变动提醒：用户{}，商品：{}，上次提醒价格：{}，当前价格：{}",
                            userId, cartInfo.getSkuName(), lastRemindedPrice, newPrice);
                } else {
                    log.info("价格未再次变动，跳过提醒：用户{}，商品：{}", userId, cartInfo.getSkuName());
                }
            } catch (NumberFormatException e) {
                // 数据异常，重新提醒
                log.warn("Redis中价格数据异常，重新提醒：用户{}，商品：{}", userId, cartInfo.getSkuName());
                shouldRemind = true;
            }
        }
        
        if (!shouldRemind) {
            return;
        }
        
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setUserId(userId);
        messageInfo.setTitle("降价提醒");
        messageInfo.setContent(String.format("您关注的商品【%s】价格已从¥%.2f降至¥%.2f，快来抢购吧！",
                cartInfo.getSkuName(), oldPrice.doubleValue(), newPrice.doubleValue()));
        messageInfo.setMessageType(4); // 4-降价提醒
        messageInfo.setIsRead(0);
        messageInfo.setBusinessId(cartInfo.getSkuId());
        // 设置数据库去重标识（与Redis key保持一致）
        messageInfo.setDedupKey("price_drop_" + cartInfo.getSkuId());
        messageInfo.setCreateTime(new Date());
        messageInfo.setUpdateTime(new Date());

        try {
            // 保存消息（带去重检查）
            Boolean saved = userFeignClient.saveMessageWithDedup(messageInfo);
            
            if (saved) {
                // 记录当前价格为已提醒价格，8小时后过期
                // 存储当前价格，用于下次对比是否有新的价格变动
                redisTemplate.opsForValue().set(dedupKey, newPrice.toString(), DEDUP_HOURS, TimeUnit.HOURS);
                
                log.info("发送降价提醒给用户{}，商品：{}，原价：{}，现价：{}", userId, cartInfo.getSkuName(), oldPrice, newPrice);
            } else {
                log.info("数据库去重拦截：用户{}，商品：{}", userId, cartInfo.getSkuName());
            }
        } catch (Exception e) {
            log.error("发送降价提醒消息失败", e);
        }
    }

    /**
     * 发送库存预警消息
     */
    private void sendStockWarningMessage(Long userId, CartInfo cartInfo, Integer stockNum) {
        // 构建去重Key：remind:stock:{userId}:{skuId}
        String dedupKey = "remind:stock:" + userId + ":" + cartInfo.getSkuId();
        
        // 获取上次提醒时记录的库存数量
        String lastRemindedStockStr = redisTemplate.opsForValue().get(dedupKey);
        
        // 判断是否需要发送提醒
        boolean shouldRemind = false;
        
        if (lastRemindedStockStr == null) {
            // 首次库存预警
            shouldRemind = true;
            log.info("首次库存预警：用户{}，商品：{}", userId, cartInfo.getSkuName());
        } else {
            // 已有提醒记录，检查库存是否有新的变动
            try {
                Integer lastRemindedStock = Integer.parseInt(lastRemindedStockStr);
                // 如果当前库存与上次提醒的库存不同，说明库存又有新变动
                if (!stockNum.equals(lastRemindedStock)) {
                    shouldRemind = true;
                    log.info("库存再次变动预警：用户{}，商品：{}，上次提醒库存：{}，当前库存：{}",
                            userId, cartInfo.getSkuName(), lastRemindedStock, stockNum);
                } else {
                    log.info("库存未再次变动，跳过预警：用户{}，商品：{}", userId, cartInfo.getSkuName());
                }
            } catch (NumberFormatException e) {
                // 数据异常，重新预警
                log.warn("Redis中库存数据异常，重新预警：用户{}，商品：{}", userId, cartInfo.getSkuName());
                shouldRemind = true;
            }
        }
        
        if (!shouldRemind) {
            return;
        }
        
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setUserId(userId);
        messageInfo.setTitle("库存预警");
        messageInfo.setContent(String.format("您关注的商品【%s】库存仅剩%d件，即将售罄，请尽快下单！",
                cartInfo.getSkuName(), stockNum));
        messageInfo.setMessageType(5); // 5-库存预警
        messageInfo.setIsRead(0);
        messageInfo.setBusinessId(cartInfo.getSkuId());
        // 设置数据库去重标识（与Redis key保持一致）
        messageInfo.setDedupKey("stock_warning_" + cartInfo.getSkuId());
        messageInfo.setCreateTime(new Date());
        messageInfo.setUpdateTime(new Date());

        try {
            // 保存消息（带去重检查）
            Boolean saved = userFeignClient.saveMessageWithDedup(messageInfo);
            
            if (saved) {
                // 记录当前库存为已提醒库存，8小时后过期
                redisTemplate.opsForValue().set(dedupKey, stockNum.toString(), DEDUP_HOURS, TimeUnit.HOURS);
                
                log.info("发送库存预警给用户{}，商品：{}，库存：{}", userId, cartInfo.getSkuName(), stockNum);
            } else {
                log.info("数据库去重拦截：用户{}，商品：{}", userId, cartInfo.getSkuName());
            }
        } catch (Exception e) {
            log.error("发送库存预警消息失败", e);
        }
    }

    /**
     * 发送售罄提醒消息
     */
    private void sendSoldOutMessage(Long userId, CartInfo cartInfo) {
        // 构建去重Key：remind:soldout:{userId}:{skuId}
        String dedupKey = "remind:soldout:" + userId + ":" + cartInfo.getSkuId();
        
        // 检查是否已经提醒过（售罄状态不需要记录具体值，只检查是否存在）
        Boolean alreadyReminded = redisTemplate.hasKey(dedupKey);
        if (alreadyReminded) {
            log.info("售罄提醒已去重：用户{}，商品：{}", userId, cartInfo.getSkuName());
            return;
        }
        
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setUserId(userId);
        messageInfo.setTitle("商品售罄提醒");
        messageInfo.setContent(String.format("您关注的商品【%s】已售罄，请关注补货通知！",
                cartInfo.getSkuName()));
        messageInfo.setMessageType(5); // 5-库存预警
        messageInfo.setIsRead(0);
        messageInfo.setBusinessId(cartInfo.getSkuId());
        // 设置数据库去重标识
        messageInfo.setDedupKey("sold_out_" + cartInfo.getSkuId());
        messageInfo.setCreateTime(new Date());
        messageInfo.setUpdateTime(new Date());

        try {
            // 保存消息（带去重检查）
            Boolean saved = userFeignClient.saveMessageWithDedup(messageInfo);
            
            if (saved) {
                // 记录已提醒，8小时后过期
                redisTemplate.opsForValue().set(dedupKey, "1", DEDUP_HOURS, TimeUnit.HOURS);
                
                log.info("发送售罄提醒给用户{}，商品：{}", userId, cartInfo.getSkuName());
            } else {
                log.info("数据库去重拦截：用户{}，商品：{}", userId, cartInfo.getSkuName());
            }
        } catch (Exception e) {
            log.error("发送售罄提醒消息失败", e);
        }
    }

}
