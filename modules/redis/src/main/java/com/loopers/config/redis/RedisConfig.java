package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.function.Supplier;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private final RedisProperties redisProperties;

    public static final String CONNECTION_MASTER = "redisConnectionMaster";
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory() {
        // 기본 ConnectionFactory: Replica 우선 읽기
        return createLettuceConnectionFactory(() -> LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build());
    }

    @Bean(name = CONNECTION_MASTER)
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        // Master ConnectionFactory: Master에서만 읽기/쓰기
        return createLettuceConnectionFactory(() -> LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.MASTER)
                .build());
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        return createRedisTemplate(connectionFactory);
    }

    @Bean(name = REDIS_TEMPLATE_MASTER)
    public RedisTemplate<String, Object> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory connectionFactory
    ) {
        return createRedisTemplate(connectionFactory);
    }

    private LettuceConnectionFactory createLettuceConnectionFactory(Supplier<LettuceClientConfiguration> clientConfigurationSupplier) {
        RedisStaticMasterReplicaConfiguration masterReplicaConfig = new RedisStaticMasterReplicaConfiguration(
                redisProperties.master().host(),
                redisProperties.master().port()
        );
        masterReplicaConfig.setDatabase(redisProperties.database());

        redisProperties.replicas().forEach(replica ->
                masterReplicaConfig.addNode(replica.host(), replica.port())
        );

        return new LettuceConnectionFactory(masterReplicaConfig, clientConfigurationSupplier.get());
    }

    private RedisTemplate<String, Object> createRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
