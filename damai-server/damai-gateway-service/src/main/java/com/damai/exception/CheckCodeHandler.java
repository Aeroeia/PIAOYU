package com.damai.exception;

import com.damai.core.StringUtil;
import com.damai.enums.BaseCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.damai.constant.GatewayConstant.CODE;

@Component
public class CheckCodeHandler {
    
    private final static String EXCEPTION_MESSAGE = "code参数为空";

    public void checkCode(String code){
        if (StringUtil.isEmpty(code)) {
            ArgumentError argumentError = new ArgumentError();
            argumentError.setArgumentName(CODE);
            argumentError.setMessage(EXCEPTION_MESSAGE);
            List<ArgumentError> argumentErrorList = new ArrayList<>();
            argumentErrorList.add(argumentError);
            throw new ArgumentException(BaseCode.ARGUMENT_EMPTY.getCode(),argumentErrorList);
        }
    }
}
