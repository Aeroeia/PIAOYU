package com.damai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.entity.TicketCategory;
import com.damai.entity.TicketCategoryAggregate;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TicketCategoryMapper extends BaseMapper<TicketCategory> {
    
    /**
     * 票档统计
     * @param programIdList 参数
     * @return 结果
     * */
    List<TicketCategoryAggregate> selectAggregateList(@Param("programIdList")List<Long> programIdList);
    
    /**
     * 更新数量
     * @param number 数量
     * @param id id
     * @return 结果
     * */
    int updateRemainNumber(@Param("number")Long number,@Param("id")Long id);
    
    /**
     * 批量更新数量
     * @param ticketCategoryCountMap 参数
     * @return 结果
     * */
    int batchUpdateRemainNumber(@Param("ticketCategoryCountMap") Map<Long, Long> ticketCategoryCountMap);
}
