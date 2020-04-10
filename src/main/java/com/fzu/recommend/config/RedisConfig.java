package com.fzu.recommend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    //用redisTemplate访问redis
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>(); //实例化bean
        template.setConnectionFactory(factory); //注入连接工厂，使其能够访问数据库
        //设置序列化方式
        //key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //hash的key序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //hash的value序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //设置生效
        template.afterPropertiesSet();
        return template;
    }
}
