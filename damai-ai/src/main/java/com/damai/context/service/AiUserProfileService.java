package com.damai.context.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.damai.entity.AiUserProfile;
import com.damai.mapper.AiUserProfileMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户画像存储服务：提供按 key 的 upsert 能力。
 * 当前仅承接低风险属性，采用“逐次小幅增信”的轻量置信度策略。
 */
@Service
public class AiUserProfileService {

    private final AiUserProfileMapper aiUserProfileMapper;

    public AiUserProfileService(AiUserProfileMapper aiUserProfileMapper) {
        this.aiUserProfileMapper = aiUserProfileMapper;
    }

    public void upsert(Long userId, String key, String value, String source) {
        if (userId == null || value == null || value.isBlank()) {
            return;
        }
        AiUserProfile profile = aiUserProfileMapper.selectOne(new LambdaQueryWrapper<AiUserProfile>()
                .eq(AiUserProfile::getUserId, userId)
                .eq(AiUserProfile::getAttrKey, key));

        if (profile == null) {
            profile = new AiUserProfile();
            profile.setUserId(userId);
            profile.setAttrKey(key);
            profile.setAttrValue(value);
            profile.setConfidence(new BigDecimal("0.70"));
            profile.setSource(source);
            profile.setStatus(1);
            aiUserProfileMapper.insert(profile);
            return;
        }

        profile.setAttrValue(value);
        // 同一属性重复命中时逐步提升置信度，并限制上限避免无限增长。
        BigDecimal newConfidence = profile.getConfidence() == null
                ? new BigDecimal("0.70")
                : profile.getConfidence().add(new BigDecimal("0.05")).min(new BigDecimal("0.95"));
        profile.setConfidence(newConfidence);
        profile.setSource(source);
        aiUserProfileMapper.updateById(profile);
    }

    public Map<String, String> getWhitelistedProfile(Long userId, List<String> keys) {
        if (userId == null || keys == null || keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<AiUserProfile> profiles = aiUserProfileMapper.selectList(new LambdaQueryWrapper<AiUserProfile>()
                .eq(AiUserProfile::getUserId, userId)
                .in(AiUserProfile::getAttrKey, keys));
        if (profiles == null || profiles.isEmpty()) {
            return Collections.emptyMap();
        }

        // 按 keys 的传入顺序返回，保证 Prompt 注入稳定可预期。
        Map<String, String> result = new LinkedHashMap<>();
        for (String key : keys) {
            for (AiUserProfile profile : profiles) {
                if (key.equals(profile.getAttrKey()) && profile.getAttrValue() != null && !profile.getAttrValue().isBlank()) {
                    result.put(key, profile.getAttrValue());
                    break;
                }
            }
        }
        return result;
    }
}
