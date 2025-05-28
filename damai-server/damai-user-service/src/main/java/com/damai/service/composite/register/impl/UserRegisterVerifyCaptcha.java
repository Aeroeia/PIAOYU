package com.damai.service.composite.register.impl;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.damai.core.StringUtil;
import com.damai.dto.UserRegisterDto;
import com.damai.exception.DaMaiFrameException;
import com.damai.service.CaptchaHandle;
import com.damai.service.composite.register.AbstractUserRegisterCheckHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserRegisterVerifyCaptcha extends AbstractUserRegisterCheckHandler {
    
    @Autowired
    private CaptchaHandle captchaHandle;
    
    /**
     * 验证验证码是否正确
     * */
    @Override
    protected void execute(UserRegisterDto param) {
        if (StringUtil.isNotEmpty(param.getCaptchaType())) {
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaType(param.getCaptchaType());
            captchaVO.setPointJson(param.getPointJson());
            captchaVO.setToken(param.getToken());
            ResponseModel responseModel = captchaHandle.checkCaptcha(captchaVO);
            if (!responseModel.isSuccess()) {
                throw new DaMaiFrameException(responseModel.getRepCode(),responseModel.getRepMsg());
            }
        }
    }
    
    @Override
    public Integer executeParentOrder() {
        return 1;
    }
    
    @Override
    public Integer executeTier() {
        return 2;
    }
    
    @Override
    public Integer executeOrder() {
        return 1;
    }
}
