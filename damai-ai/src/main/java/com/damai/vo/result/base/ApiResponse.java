package com.damai.vo.result.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ApiResponse {

    @Schema(name ="code", type ="Integer", description ="响应码 0:成功 其余:失败")
    private Integer code;

    @Schema(name ="message", type ="String", description ="错误信息")
    private String message;
}
