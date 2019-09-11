package com.aihuishou.bi;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class Config {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setInitialSize(10);
        dataSource.setMinIdle(20);
        dataSource.setMaxActive(150);
        dataSource.setValidationQuery("select 1");
        //空闲时检查连接是否有效
        dataSource.setTestWhileIdle(true);
        //借出连接时不要测试，否则很影响性能
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setRemoveAbandoned(true);
        //60秒进行一次检测，检测需要关闭的空闲连接
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(30000);
        dataSource.setMaxEvictableIdleTimeMillis(180000);
        //1800秒，也就是30分钟
        dataSource.setRemoveAbandonedTimeout(1800);
        //连接最大存活时间，默认是-1(不限制物理连接时间)，从创建连接开始计算，如果超过该时间，则会被清理
        dataSource.setPhyTimeoutMillis(15000);
        //指明连接是否被空闲连接回收器(如果有)进行检验.如果检测失败,则连接将被从池中去除.
        dataSource.setTestWhileIdle(true);
        return dataSource;
    }
}
