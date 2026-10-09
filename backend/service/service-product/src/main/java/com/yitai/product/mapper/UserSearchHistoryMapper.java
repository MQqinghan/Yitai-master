package com.yitai.product.mapper;

import com.yitai.model.entity.product.UserSearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserSearchHistoryMapper {

    /**
     * 添加或更新搜索记录
     */
    void insertOrUpdate(UserSearchHistory searchHistory);

    /**
     * 统计用户搜索最多的关键词
     */
    List<Map<String, Object>> selectTopKeywords(@Param("userId") Long userId, @Param("limit") Integer limit);
}
