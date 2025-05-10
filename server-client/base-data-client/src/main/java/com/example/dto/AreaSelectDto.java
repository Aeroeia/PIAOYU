package com.example.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value="AreaSelectDto", description ="AreaSelectDto")
public class AreaSelectDto {
    
    @ApiModelProperty(name ="idList", dataType ="List<Long>", value ="id集合", required =true)
    @NotNull
    private List<Long> idList;
}
