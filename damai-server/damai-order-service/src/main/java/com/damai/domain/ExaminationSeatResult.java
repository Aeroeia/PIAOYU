package com.damai.domain;

import com.damai.entity.OrderTicketUserRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationSeatResult {

    /**
     * 以redis为准的座位记录统计数量
     * */
    private int redisStandardStatisticCount;
    
    /**
     * 以数据库为准的座位记录统计数量
     * */
    private int dbStandardStatisticCount;

    /**
     * 需要向数据库中补充的座位
     * */
    private List<SeatRecord> needToDbSeatRecordList;

    /**
     * 需要向redis中补充的座位
     * */
    private List<OrderTicketUserRecord> needToRedisSeatRecordList;
}
