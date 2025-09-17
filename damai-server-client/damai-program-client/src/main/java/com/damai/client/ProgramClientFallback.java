package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.ReduceRemainNumberDto;
import com.damai.enums.BaseCode;
import org.springframework.stereotype.Component;


@Component
public class ProgramClientFallback implements ProgramClient {

    @Override
    public ApiResponse<Boolean> operateSeatLockAndTicketCategoryRemainNumber(final ReduceRemainNumberDto reduceRemainNumberDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
