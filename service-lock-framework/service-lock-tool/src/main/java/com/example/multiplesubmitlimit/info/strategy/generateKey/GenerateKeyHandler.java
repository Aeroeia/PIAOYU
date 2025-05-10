package com.example.multiplesubmitlimit.info.strategy.generateKey;

import org.aspectj.lang.JoinPoint;

public interface GenerateKeyHandler {

    String generateKey(JoinPoint joinPoint);
}
