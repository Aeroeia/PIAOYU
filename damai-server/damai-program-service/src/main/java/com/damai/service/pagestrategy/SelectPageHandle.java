package com.damai.service.pagestrategy;

import com.damai.dto.ProgramPageListDto;
import com.damai.page.PageVo;
import com.damai.vo.ProgramListVo;
public interface SelectPageHandle {
    /**
     * 分页查询
     * @param dto 参数
     * @return 结果
     * */
    PageVo<ProgramListVo> selectPage(ProgramPageListDto dto);
    
    /**
     * 获取分页查询类型
     * @return 结果
     * */
    String getType();
}
