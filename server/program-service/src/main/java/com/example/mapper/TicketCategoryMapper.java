package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.TicketCategory;
import com.example.entity.TicketCategoryAggregate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TicketCategoryMapper extends BaseMapper<TicketCategory> {

    List<TicketCategoryAggregate> selectAggregateList(@Param("programIdList")List<Long> programIdList);
    
    int updateRemainNumber(@Param("number")Long number,@Param("id")Long id);
}
