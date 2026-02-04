package com.damai;

import org.dromara.easyes.spring.annotation.EsMapperScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EsMapperScan("com.damai.es.mapper")
@MapperScan("com.damai.mapper")
@SpringBootApplication
public class DaMaiAiCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaMaiAiCoreApplication.class, args);
    }

}
