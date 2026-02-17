package com.damai.config;

import com.damai.tool.LogQueryMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class McpLogConfig {

    /**
     * 注册MCP工具回调提供者
     * 把LogQueryMcpTool中的@Tool方法注册为MCP可调用的工具
     */
    @Bean
    public ToolCallbackProvider logQueryToolCallbackProvider(LogQueryMcpTool logQueryMcpTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(logQueryMcpTool)
                .build();
    }
}
