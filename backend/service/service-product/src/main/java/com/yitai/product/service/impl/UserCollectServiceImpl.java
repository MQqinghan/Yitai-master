package com.yitai.product.service.impl;

import com.yitai.model.entity.product.ProductSku;
import com.yitai.model.entity.product.UserCollect;
import com.yitai.model.vo.product.UserCollectVo;
import com.yitai.product.mapper.ProductSkuMapper;
import com.yitai.product.mapper.UserCollectMapper;
import com.yitai.product.service.UserCollectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserCollectServiceImpl implements UserCollectService {

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Autowired
    private ProductSkuMapper productSkuMapper;

    // 收藏商品
    @Override
    public void collect(Long userId, Long skuId) {
        // 判断是否已经收藏
        UserCollect existCollect = userCollectMapper.getByUserIdAndSkuId(userId, skuId);
        if (existCollect != null) {
            // 如果已收藏，不做任何操作
            return;
        }

        // 创建收藏记录
        UserCollect userCollect = new UserCollect();
        userCollect.setUserId(userId);
        userCollect.setSkuId(skuId);
        userCollectMapper.save(userCollect);
    }

    // 取消收藏
    @Override
    public void cancelCollect(Long userId, Long skuId) {
        userCollectMapper.deleteByUserIdAndSkuId(userId, skuId);
    }

    // 获取收藏列表
    @Override
    public List<UserCollectVo> getCollectList(Long userId) {
        // 获取用户收藏记录
        List<UserCollect> userCollectList = userCollectMapper.getListByUserId(userId);

        // 封装VO列表
        List<UserCollectVo> userCollectVoList = new ArrayList<>();
        for (UserCollect userCollect : userCollectList) {
            // 根据skuId获取商品信息
            ProductSku productSku = productSkuMapper.getById(userCollect.getSkuId());
            if (productSku != null) {
                UserCollectVo userCollectVo = new UserCollectVo();
                userCollectVo.setId(userCollect.getId());
                userCollectVo.setSkuId(productSku.getId());
                userCollectVo.setSkuName(productSku.getSkuName());
                userCollectVo.setThumbImg(productSku.getThumbImg());
                userCollectVo.setSalePrice(productSku.getSalePrice());
                userCollectVo.setMarketPrice(productSku.getMarketPrice());
                userCollectVo.setStockNum(productSku.getStockNum());
                userCollectVoList.add(userCollectVo);
            }
        }

        return userCollectVoList;
    }
}
