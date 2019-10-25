package com.aihuishou.bi;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

@Configuration
public class BasicMongoConfigure {

    @Value("${basic.mongodb.host}")
    private String host;
    @Value("${basic.mongodb.database}")
    private String database;
    @Value("${basic.mongodb.port}")
    private int port;


    @Bean(name = "basicMongoTemplate")
    public MongoTemplate getMongoTemplate() throws Exception {
        //连接池中某个连接的空闲时间超过该值，将丢弃该连接并重新新建立一个连接
        MongoClientOptions options = MongoClientOptions.builder().maxConnectionIdleTime(60000).build();
        MongoClient client = new MongoClient(new ServerAddress(host, port), options);
        return new MongoTemplate(new SimpleMongoDbFactory(client, database));
    }
}
