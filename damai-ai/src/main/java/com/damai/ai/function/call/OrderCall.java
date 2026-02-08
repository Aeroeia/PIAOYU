package com.damai.ai.function.call;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;

import com.damai.dto.ProgramOrderCreateDto;
import com.damai.enums.BaseCode;
import com.damai.vo.result.CreateOrderResult;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.damai.constants.DaMaiConstant.CREATE_ORDER_URL;


@Component
public class OrderCall {
    
    public String createOrder(ProgramOrderCreateDto programOrderCreateDto){
        CreateOrderResult createOrderResult = new CreateOrderResult();
        String result = HttpRequest.post(CREATE_ORDER_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(programOrderCreateDto))
                .timeout(20000)
                .execute().body();
        createOrderResult = JSON.parseObject(result, CreateOrderResult.class);
        if (!Objects.equals(createOrderResult.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统创建订单失败");
        }
        return createOrderResult.getData();
    }
}
