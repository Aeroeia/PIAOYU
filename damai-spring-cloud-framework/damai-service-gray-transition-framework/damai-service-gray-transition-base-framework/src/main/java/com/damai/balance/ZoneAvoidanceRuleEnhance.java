package com.damai.balance;


import com.google.common.base.Optional;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import java.util.ArrayList;
import java.util.List;

public class ZoneAvoidanceRuleEnhance extends ZoneAvoidanceRule {
    

    @Override
    public Server choose(Object key) {
        ILoadBalancer loadBalancer = getLoadBalancer();
        List<Server> allServers = new ArrayList<>();
        java.util.Optional.ofNullable(loadBalancer.getAllServers()).ifPresent(allServers::addAll);
        Optional<Server> serverOptional = getPredicate().chooseRoundRobinAfterFiltering(allServers, key);
        return serverOptional.isPresent() ? serverOptional.get() : null;
    }
}