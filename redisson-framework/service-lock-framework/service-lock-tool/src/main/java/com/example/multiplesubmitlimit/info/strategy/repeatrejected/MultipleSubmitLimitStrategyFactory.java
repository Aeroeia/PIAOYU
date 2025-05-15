package com.example.multiplesubmitlimit.info.strategy.repeatrejected;

import com.example.enums.BaseCode;
import com.example.exception.CookFrameException;

import java.util.Optional;

public class MultipleSubmitLimitStrategyFactory {

    public MultipleSubmitLimitHandler getMultipleSubmitLimitStrategy(String key){
        return Optional.ofNullable(MultipleSubmitLimitStrategyContext.get(key))
                .orElseThrow(() -> new CookFrameException(BaseCode.REJECT_STRATEGY_NOT_EXIST));
    }
}
