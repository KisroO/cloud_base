package com.kisro.cloud.manage.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kisro
 * @since 2022/10/28
 **/
@Slf4j
@Configuration
//@EnableConfigurationProperties(HBaseProperties.class)
public class HBaseConfig {
//    private final HBaseProperties properties;
//
//    public HBaseConfig(HBaseProperties properties) {
//        this.properties = properties;
//    }

    public org.apache.hadoop.conf.Configuration configuration() {
        org.apache.hadoop.conf.Configuration hbaseConfig = HBaseConfiguration.create();
        hbaseConfig.set("hbase.master", "172.28.0.34:2181");
        hbaseConfig.set("hbase.zookeeper.quorum", "172.28.0.34:2181");
//        Map<String, Object> customConfig = properties.getConfig();
//        Set<String> keySet = customConfig.keySet();
//        keySet.forEach(k-> hbaseConfig.set(k,String.valueOf(customConfig.get(k))));
        return hbaseConfig;
    }
}
