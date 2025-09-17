package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.ReduceRemainNumberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import static com.damai.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;


@Component
@FeignClient(value = SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"program-service",fallback = ProgramClientFallback.class)
public interface ProgramClient {

    /**
     * 更新座位为锁定和扣减余票数量
     * @param reduceRemainNumberDto 参数
     * @return 结果
     * */
    @PostMapping("/program/interior/reduce/remain/number")
    ApiResponse<Boolean> operateSeatLockAndTicketCategoryRemainNumber(ReduceRemainNumberDto reduceRemainNumberDto);

}
