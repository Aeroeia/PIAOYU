package com.damai.ai.function.call;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.damai.enums.BaseCode;
import com.damai.vo.TicketUserVo;
import com.damai.vo.UserDetailVo;
import com.damai.vo.result.TicketUserResultVo;
import com.damai.vo.result.UserDetailResultVo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.damai.constants.DaMaiConstant.TICKET_USER_LIST_URL;
import static com.damai.constants.DaMaiConstant.USER_DETAIL_URL;

@Component
public class UserCall {
    public UserDetailVo userDetail(String mobile){
        Map<String,String> params = new HashMap<>(2);
        params.put("mobile", mobile);
        UserDetailResultVo userDetailResultVo = new UserDetailResultVo();
        String result = HttpRequest.post(USER_DETAIL_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(params))
                .timeout(20000)
                .execute().body();
        userDetailResultVo = JSON.parseObject(result, UserDetailResultVo.class);
        if (!Objects.equals(userDetailResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询用户信息失败");
        }
        return userDetailResultVo.getData();
    }
    public List<TicketUserVo> ticketUserList(Long userId){
        Map<String,Object> params = new HashMap<>(2);
        params.put("userId", userId);
        TicketUserResultVo ticketUserResultVo = new TicketUserResultVo();
        String result = HttpRequest.post(TICKET_USER_LIST_URL)
                .header("no_verify", "true")
                .body(JSON.toJSONString(params))
                .timeout(20000)
                .execute().body();
        ticketUserResultVo = JSON.parseObject(result, TicketUserResultVo.class);
        if (!Objects.equals(ticketUserResultVo.getCode(), BaseCode.SUCCESS.getCode())) {
            throw new RuntimeException("调用大麦系统查询购票人信息失败");
        }
        if (Objects.isNull(ticketUserResultVo.getData())) {
            throw new RuntimeException("购票人信息不存在");
        }
        return ticketUserResultVo.getData();
    }

}
