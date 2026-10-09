package com.yitai.manager.service.impl;

import com.yitai.manager.mapper.ProductUnitMapper;
import com.yitai.manager.service.ProductUnitService;
import com.yitai.model.entity.base.ProductUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductUnitServiceImpl implements ProductUnitService {

    @Autowired
    private ProductUnitMapper productUnitMapper ;

    @Override
    public List<ProductUnit> findAll() {
        return productUnitMapper.findAll();
    }
}
