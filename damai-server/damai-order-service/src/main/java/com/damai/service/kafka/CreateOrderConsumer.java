package com.damai.service.kafka;

import com.alibaba.fastjson.JSON;
import com.damai.dto.OrderCreateDto;
import com.damai.service.OrderService;
import com.damai.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Component
public class CreateOrderConsumer {
    
    @Autowired
    private OrderService orderService;
    
    @KafkaListener(topics = {"${spring.kafka.topic:create_order}"})
    public void consumerOrderMessage(ConsumerRecord<String,String> consumerRecord){
        try {
            Optional.ofNullable(consumerRecord.value()).map(String::valueOf).ifPresent(value -> {
                log.info("consumerOrderMessage message start message: {}",value);
                OrderCreateDto orderCreateDto = JSON.parseObject(value, OrderCreateDto.class);
                
                long createOrderTimeTimestamp = DateUtils.getDateTimeStampNo(orderCreateDto.getCreateOrderTime());
                
                long currentTimeTimestamp = System.currentTimeMillis();
                
                String orderNumber = orderService.createByMq(orderCreateDto);
                log.info("consumerOrderMessage message end orderNumber: {}",orderNumber);
            });
        }catch (Exception e) {
            log.error("consumerOrderMessage error",e);
        }
    }
}
