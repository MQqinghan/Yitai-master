package com.yitai.cart.task;

import com.yitai.cart.service.SmartDecisionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmartDecisionTask {

    @Autowired
    private SmartDecisionService smartDecisionService;

    /**
     * 定时检查购物车商品的价格和库存变化
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 300000毫秒 = 5分钟
    public void checkCartSmartDecision() {
        log.info("========== 定时任务：开始执行购物车智能决策检查 ==========");
        try {
            smartDecisionService.executeSmartDecisionCheck();
        } catch (Exception e) {
            log.error("定时任务：智能决策检查执行失败", e);
        }
        log.info("========== 定时任务：购物车智能决策检查完成 ==========");
    }

}
