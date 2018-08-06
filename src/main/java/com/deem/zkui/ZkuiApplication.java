package com.deem.zkui;

import cn.hutool.setting.dialect.Props;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @program: zkui1
 * @description:
 * @author: guoqingming
 * @create: 2018-08-03 22:02
 **/
@SpringBootApplication
@MapperScan("com.deem.zkui.mapper")
public class ZkuiApplication {

    @Bean
    public CuratorFramework curatorFramework() {
        Props props = new Props("application.properties");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(props.getStr("zk.url"), retryPolicy);
        client.start();
        return client;
    }
    public static void main(String[] args) {
        SpringApplication.run(ZkuiApplication.class);
    }
}
