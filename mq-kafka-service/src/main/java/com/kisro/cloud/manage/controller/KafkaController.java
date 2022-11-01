package com.kisro.cloud.manage.controller;

import com.kisro.cloud.manage.service.KafkaSender;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kisro
 * @since 2022/10/27
 **/
@RestController
@RequestMapping("/message")
@AllArgsConstructor
public class KafkaController {
    private KafkaSender kafkaSender;

    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        kafkaSender.sendObj(message);
        return "message has sent";
    }
}
