package com.damai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.damai.entity.base.BaseTableData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("ai_user_profile")
public class AiUserProfile extends BaseTableData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String attrKey;

    private String attrValue;

    private BigDecimal confidence;

    private String source;
}
