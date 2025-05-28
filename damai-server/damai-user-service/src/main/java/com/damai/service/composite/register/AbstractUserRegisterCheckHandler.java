package com.damai.service.composite.register;

import com.damai.composite.AbstractComposite;
import com.damai.dto.UserRegisterDto;
import com.damai.enums.CompositeCheckType;

public abstract class AbstractUserRegisterCheckHandler extends AbstractComposite<UserRegisterDto> {
    
    @Override
    public String type() {
        return CompositeCheckType.USER_REGISTER_CHECK.getValue();
    }
}
