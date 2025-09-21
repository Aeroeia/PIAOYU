package com.damai.client;

import com.damai.common.ApiResponse;
import com.damai.dto.ProgramOperateDataDto;
import com.damai.dto.ProgramRecordTaskAddDto;
import com.damai.dto.ProgramRecordTaskListDto;
import com.damai.dto.ProgramRecordTaskUpdateDto;
import com.damai.dto.ReduceRemainNumberDto;
import com.damai.dto.TicketCategoryListDto;
import com.damai.enums.BaseCode;
import com.damai.vo.ProgramRecordTaskVo;
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

    @Override
    public ApiResponse<List<ProgramRecordTaskVo>> select(final ProgramRecordTaskListDto programRecordTaskListDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<Integer> update(final ProgramRecordTaskUpdateDto programRecordTaskUpdateDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<Integer> add(final ProgramRecordTaskAddDto orderTicketUserRecordAddDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    @Override
    public ApiResponse<Boolean> operateProgramData(final ProgramOperateDataDto programOperateDataDto) {
        return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }
}
