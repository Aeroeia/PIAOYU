package com.damai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.entity.OrderTicketUser;
import com.damai.entity.OrderTicketUserAggregate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderTicketUserMapper extends BaseMapper<OrderTicketUser> {
    
    /**
     * 查询订单下购票人数量
     * @param orderNumberList 参数
     * @return 结果
     * */
    List<OrderTicketUserAggregate> selectOrderTicketUserAggregate(@Param("orderNumberList")List<Long> orderNumberList);

}
