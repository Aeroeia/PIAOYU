package com.damai.context.model;

public enum AiOrderExecutionStatus {
    INIT,
    CHECK,
    LOCK,
    PAY,
    CONFIRM,
    DONE,
    FAILED,
    WAITING_USER_INPUT
}
