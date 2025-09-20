package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.ReduceRemainNumberDto;
import com.damai.dto.TicketCategoryListDto;
import com.damai.enums.BaseCode;
import com.damai.vo.TicketCategoryDetailVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramClientFallback implements ProgramClient {

    @Override
    public ApiResponse<Boolean> operateSeatLockAndTicketCategoryRemainNumber(final ReduceRemainNumberDto reduceRemainNumberDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<List<TicketCategoryDetailVo>> selectList(final TicketCategoryListDto ticketCategoryDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<List<Long>> allList() {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
