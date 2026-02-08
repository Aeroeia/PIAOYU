package com.damai.vo.result;

import com.damai.vo.TicketUserVo;
import com.damai.vo.result.base.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class TicketUserResultVo extends ApiResponse implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private List<TicketUserVo> data;
}
