package com.damai.vo.result;

import com.damai.vo.result.base.ApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;


@EqualsAndHashCode(callSuper = true)
@Data
public class CreateOrderResult extends ApiResponse implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String data;
}
