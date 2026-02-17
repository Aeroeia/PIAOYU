package com.damai;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@EsMapperScan("com.damai.mapper")
public class DaMaiMcpLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaMaiMcpLogApplication.class, args);
    }
}
