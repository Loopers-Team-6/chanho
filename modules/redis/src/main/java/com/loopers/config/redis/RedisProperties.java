package com.loopers.config.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "datasource.redis")
public record RedisProperties(
        int database,
        RedisNodeInfo master,
        List<RedisNodeInfo> replicas
) {
}
