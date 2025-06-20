package com.damai.balance.config;

import org.springframework.cloud.netflix.ribbon.RibbonClients;


@RibbonClients(defaultConfiguration = { WorkLoadBalanceConfiguration.class })
public class ExtraRibbonAutoConfiguration {
}
