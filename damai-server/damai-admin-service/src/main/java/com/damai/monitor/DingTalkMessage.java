package com.damai.monitor;

import com.alibaba.fastjson.JSON;
import com.damai.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DingTalkMessage {
    
    private final String token;
    
    private RestTemplate restTemplate = new RestTemplate();
    
    private HttpEntity<String> createMessage(String message) {
        Map<String, Object> messageJson = new HashMap<>(8);
        Map<String, Object> context = new HashMap<>(8);
        context.put("content", message);
        messageJson.put("text", JSON.toJSONString(context));
        messageJson.put("msgtype", "text");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(JSON.toJSONString(messageJson), headers);
    }
    
    public void sendMessage(String message){
        if (StringUtil.isNotEmpty(token)) {
            restTemplate.postForEntity(token, createMessage(message), Void.class);
        }
    }
}
