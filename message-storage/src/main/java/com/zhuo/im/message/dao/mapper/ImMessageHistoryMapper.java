package com.zhuo.im.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuo.im.message.dao.ImMessageHistoryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {

    /**
     * Batch insert (mysql)
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);
}
