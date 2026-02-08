package com.damai.vo.result;

import com.damai.vo.ProgramDetailVo;
import com.damai.vo.result.base.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;


@EqualsAndHashCode(callSuper = true)
@Data
public class ProgramDetailResultVo extends ApiResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ProgramDetailVo data;
}
