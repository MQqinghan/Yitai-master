package com.yitai.product.mapper;

import com.yitai.model.entity.product.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserBrowseHistoryMapper {

    /**
     * 添加或更新浏览记录
     */
    void insertOrUpdate(UserBrowseHistory browseHistory);

    /**
     * 查询用户最近浏览的SKU列表(按时间倒序)
     */
    List<Long> selectRecentSkuIds(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计用户浏览最多的类目
     */
    List<Map<String, Object>> selectTopCategories(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询用户最近浏览的SKU ID列表（按时间倒序）
     */
    List<Long> selectRecentSkuIdsWithTime(@Param("userId") Long userId, @Param("limit") Integer limit);
}
