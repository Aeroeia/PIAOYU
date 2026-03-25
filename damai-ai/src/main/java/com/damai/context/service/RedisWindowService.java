package com.damai.context.service;

import com.alibaba.fastjson.JSON;
import com.damai.context.model.WindowMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Redis 会话窗口服务：维护短期对话消息队列。
 * 设计上“保留20条、读取recent10”，为后续摘要压缩提供缓冲区。
 */
@Service
@Slf4j
public class RedisWindowService {

    /**
     * Redis窗口保留上限：用于支持"累计20条触发压缩"策略。
     * 上下文读取仍固定取最近10条，由调用方控制。
     */
    private static final int WINDOW_RETENTION_LIMIT = 20;
    private static final String WINDOW_KEY_PREFIX = "ai:session:window:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisWindowService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void append(Integer chatType, String chatId, String role, String content) {
        if (chatId == null || content == null) {
            return;
        }
        String key = buildKey(chatType, chatId);
        WindowMessage windowMessage = new WindowMessage();
        windowMessage.setRole(role);
        windowMessage.setContent(content);
        windowMessage.setTimestamp(System.currentTimeMillis());

        try {
            stringRedisTemplate.opsForList().rightPush(key, JSON.toJSONString(windowMessage));
            Long size = stringRedisTemplate.opsForList().size(key);
            while (size != null && size > WINDOW_RETENTION_LIMIT) {
                stringRedisTemplate.opsForList().leftPop(key);
                size = stringRedisTemplate.opsForList().size(key);
            }
        } catch (Exception e) {
            log.warn("append redis window failed chatType={} chatId={}", chatType, chatId, e);
        }
    }

    public List<WindowMessage> getRecent(Integer chatType, String chatId, int size) {
        if (chatId == null || size <= 0) {
            return Collections.emptyList();
        }
        String key = buildKey(chatType, chatId);
        try {
            Long total = stringRedisTemplate.opsForList().size(key);
            if (total == null || total == 0) {
                return Collections.emptyList();
            }
            // 按尾部读取，保证优先拿到最新上下文。
            long start = Math.max(0, total - size);
            List<String> values = stringRedisTemplate.opsForList().range(key, start, total - 1);
            return parseList(values);
        } catch (Exception e) {
            log.warn("getRecent redis window failed chatType={} chatId={}", chatType, chatId, e);
            return Collections.emptyList();
        }
    }

    public List<WindowMessage> getOldest(Integer chatType, String chatId, int size) {
        if (chatId == null || size <= 0) {
            return Collections.emptyList();
        }
        String key = buildKey(chatType, chatId);
        try {
            List<String> values = stringRedisTemplate.opsForList().range(key, 0, size - 1);
            return parseList(values);
        } catch (Exception e) {
            log.warn("getOldest redis window failed chatType={} chatId={}", chatType, chatId, e);
            return Collections.emptyList();
        }
    }

    public void compactKeepLast(Integer chatType, String chatId, int keepLast) {
        String key = buildKey(chatType, chatId);
        try {
            Long size = stringRedisTemplate.opsForList().size(key);
            if (size == null || size <= keepLast) {
                return;
            }
            long start = Math.max(0, size - keepLast);
            stringRedisTemplate.opsForList().trim(key, start, size - 1);
        } catch (Exception e) {
            log.warn("compact redis window failed chatType={} chatId={}", chatType, chatId, e);
        }
    }

    public void clear(Integer chatType, String chatId) {
        try {
            stringRedisTemplate.delete(buildKey(chatType, chatId));
        } catch (Exception e) {
            log.warn("clear redis window failed chatType={} chatId={}", chatType, chatId, e);
        }
    }

    private String buildKey(Integer chatType, String chatId) {
        return WINDOW_KEY_PREFIX + chatType + ":" + chatId;
    }

    private List<WindowMessage> parseList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<WindowMessage> list = new ArrayList<>(values.size());
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                list.add(JSON.parseObject(value, WindowMessage.class));
            } catch (Exception e) {
                log.warn("parse window message failed", e);
            }
        }
        return list;
    }
}
