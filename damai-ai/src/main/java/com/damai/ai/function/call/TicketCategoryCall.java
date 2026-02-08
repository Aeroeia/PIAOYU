package com.damai.ai.function.call;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;

import com.damai.dto.TicketCategoryListByProgramDto;
import com.damai.enums.BaseCode;
import com.damai.vo.TicketCategoryDetailVo;
import com.damai.vo.result.TicketCategoryListResultVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.damai.constants.DaMaiConstant.TICKET_LIST_URL;


@Component
public class TicketCategoryCall {

    public List<TicketCategoryDetailVo> selectListByProgram(TicketCategoryListByProgramDto ticketCategoryListByProgramDto) {
        String result = HttpRequest.post(TICKET_LIST_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(ticketCategoryListByProgramDto))
                .timeout(20000)
                .execute().body();
        TicketCategoryListResultVo ticketCategoryListResultVo = JSON.parseObject(result, TicketCategoryListResultVo.class);
        if (!Objects.equals(ticketCategoryListResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询票档信息失败");
        }
        if (CollectionUtil.isEmpty(ticketCategoryListResultVo.getData())) {
            throw new RuntimeException("票档信息不存在");
        }
        return ticketCategoryListResultVo.getData();
    }
}
