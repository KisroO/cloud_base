package com.kisro.cloud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kisro.cloud.constant.KafkaConstant;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Kisro
 * @since 2022/10/27
 **/
@Component
@AllArgsConstructor
public class KafkaSender {

    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStrMsg(String msg) {
        kafkaTemplate.send(KafkaConstant.TEST_TOPIC, msg);
    }

    public <T> void sendObj(T obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonObj = mapper.writeValueAsString(obj);
            kafkaTemplate.send(KafkaConstant.TEST_TOPIC, jsonObj);
        } catch (JsonProcessingException e) {
            // xxx
            e.printStackTrace();
        }
    }


}
