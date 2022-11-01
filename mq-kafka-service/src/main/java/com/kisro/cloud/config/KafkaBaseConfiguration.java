package com.kisro.cloud.config;

import com.kisro.cloud.constant.KafkaConstant;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * @author Kisro
 * @since 2022/10/27
 **/
@Configuration
@ConditionalOnClass(KafkaAdmin.class)
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaBaseConfiguration {
    private final KafkaProperties properties;

    public KafkaBaseConfiguration(KafkaProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化对kafka执行操作的对象
     *
     * @return KafkaAdmin
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(this.properties.buildConsumerProperties());
    }

    @Bean
    public AdminClient kafkaAdminClient() {
        return AdminClient.create(kafkaAdmin().getConfig());
    }

    @Bean
    public NewTopic testTopic() {
        return TopicBuilder.name(KafkaConstant.TEST_TOPIC)
                .partitions(2)
                .replicas(2)
                .compact()
                .build();
    }
}
