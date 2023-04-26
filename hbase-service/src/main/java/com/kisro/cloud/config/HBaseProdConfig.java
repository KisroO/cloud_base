package com.kisro.cloud.config;

import com.chinaway.columnar.hbase.HBaseColumnarAdmin;
import com.chinaway.columnar.hbase.HBaseColumnarClient;
import com.chinaway.columnar.hbase.HBaseSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author zoutao
 * @since 2023/3/29
 **/
@Configuration
public class HBaseProdConfig {

    @Value("${hbase.rootdir:hdfs://apps/hbase/data}")
    private String rootDir;
    @Value("${hbase.zookeeper.quorum:localhost:2181}")
    private String zkQuorum;
    @Value("${zookeeper.znode.parent:/hbase}")
    private String zkNodeParent;

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
