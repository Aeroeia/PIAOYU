package com.damai.context.filter;


import com.damai.context.impl.GatewayContextHolder;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class GatewayWorkRouteFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        GatewayContextHolder.getCurrentGatewayContext().setExchange(exchange);
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
