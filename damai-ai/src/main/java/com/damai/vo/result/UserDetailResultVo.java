package com.damai.vo.result;

import com.damai.vo.UserDetailVo;
import com.damai.vo.result.base.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serial;
import java.io.Serializable;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserDetailResultVo extends ApiResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private UserDetailVo data;

}
