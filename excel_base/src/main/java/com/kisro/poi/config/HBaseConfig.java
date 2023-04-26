package com.kisro.poi.config;

import com.chinaway.columnar.hbase.HBaseColumnarAdmin;
import com.chinaway.columnar.hbase.HBaseColumnarClient;
import com.chinaway.columnar.hbase.HBaseSource;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author zoutao
 * @since 2023/4/24
 **/
@Configuration
@Data
public class HBaseConfig {

    private String rootDir = "hdfs://apps/hbase/data";
    private String zkQuorum = "10.24.50.17:2181,10.24.50.2:2181,10.24.50.11:2181";
    private String zkNodeParent = "";

    @Bean
    public HBaseColumnarClient hBaseColumnarClient() throws Exception {
        HBaseColumnarClient client = new HBaseColumnarClient();

        Properties props = new Properties();
        props.setProperty("hbase.zookeeper.quorum", zkQuorum);

        HBaseSource source = new HBaseSource(props);
        source.getConfiguration().set("hbase.defaults.for.version.skip", "true");
        client.setHBaseSource(source);
        return client;
    }

    @Bean
    public HBaseColumnarAdmin hBaseColumnarAdmin() throws Exception {
        HBaseColumnarAdmin admin = new HBaseColumnarAdmin();

        Properties props = new Properties();
        props.setProperty("hbase.zookeeper.quorum", zkQuorum);

        HBaseSource source = new HBaseSource(props);
        source.getConfiguration().set("hbase.defaults.for.version.skip", "true");
        admin.setHBaseSource(source);
        return admin;
    }

}
