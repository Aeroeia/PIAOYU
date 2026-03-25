package com.damai.context.model;

/**
 * 通道意图：用于将请求引导到专用入口（RAG/OPS）或保持当前通道（NONE）。
 */
public enum AiChannelIntent {
    RAG,
    OPS,
    NONE
}
