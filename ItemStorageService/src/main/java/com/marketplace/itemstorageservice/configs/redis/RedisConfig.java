package com.marketplace.itemstorageservice.configs.redis;
import com.marketplace.itemstorageservice.models.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
@Slf4j
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    @Primary
    RedisStandaloneConfiguration connectionConfiguration(){
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        config.setPassword(password);
        config.setDatabase(database);
        return config;
    }

    @Bean
    public JedisConnectionFactory connectionFactory() {
        return new JedisConnectionFactory(connectionConfiguration());
    }

    @Bean(name = "ItemCacheTemplate")
    public RedisTemplate<String, Item> itemTemplate(){
        RedisTemplate<String, Item> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new ItemRedisSerializer());
        template.setHashValueSerializer(new ItemRedisSerializer());
        template.setConnectionFactory(connectionFactory());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }
}
