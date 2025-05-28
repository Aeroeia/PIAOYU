package com.damai.core;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import static com.damai.constant.Constant.SPRING_INJECT_PREFIX_DISTINCTION_NAME;

@Data
public class PrefixDistinctionNameProperties {
    
    @Value(SPRING_INJECT_PREFIX_DISTINCTION_NAME)
    private String name;
}
