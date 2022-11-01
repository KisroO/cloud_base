package com.kisro.cloud.service;

import com.kisro.cloud.constant.KafkaConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author Kisro
 * @since 2022/10/27
 **/
@Component
@Slf4j
public class KafkaConsumerListener {
    @KafkaListener(topics = KafkaConstant.TEST_TOPIC)
    public void onMessage1(String message) {
        log.info("topic: {}, consumer1 message: {}", KafkaConstant.TEST_TOPIC, message);
    }

}
