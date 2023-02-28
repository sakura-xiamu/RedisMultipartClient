package com.sakura;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * @description: Redis多库客户端
 * @author: Yang Sakura
 **/
public class RedisMultipartClient {

    /**
     * Map<库号，jedis实例>
     */
    private final static Map<Integer, JedisPool> jedisPoolMap = Maps.newConcurrentMap();

    /**
     * 这里使用单例模式
     * <br/>
     * 只允许通过 getRedisClient 获取Jedis客户端
     *
     * @see RedisMultipartClient#getRedisClient
     */
    private RedisMultipartClient() {
    }

    /**
     * 通过数据库序号获取对应的客户端
     * <br/>
     * 双重验证模式
     *
     * @param database
     * @return
     */
    public static Jedis getRedisClient(int database) {
        Jedis jedis;
        jedis = getJedis(database);
        if (jedis != null) {
            return jedis;
        }
        synchronized (RedisMultipartClient.class) {
            jedis = getJedis(database);
            if (jedis != null) {
                return jedis;
            }
            JedisPool jedisPool = instanceJedisPool(database);
            jedisPoolMap.put(database, jedisPool);
            jedis = jedisPool.getResource();
            return jedis;
        }
    }

    /**
     * 通过数据库序号获取Jedis客户端
     *
     * @param database
     * @return
     */
    private static Jedis getJedis(int database) {
        JedisPool jedisPool = jedisPoolMap.get(database);
        if (jedisPool != null) {
            Jedis jedis = jedisPool.getResource();
            return jedis;
        }
        return null;
    }

    /**
     * 实例化JedisPool
     *
     * @param databaseNum
     * @return
     */
    private static JedisPool instanceJedisPool(Integer databaseNum) {
        // 获取redis配置
        RedisProperties redisProperties = getRedisProperties();
        RedisProperties.Jedis jedis = redisProperties.getJedis();
        RedisProperties.Pool pool = jedis.getPool();
        //创建配置对象
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        config.setMaxWaitMillis(pool.getMaxWait().toMillis());

        //创建jedis连接池对象
        JedisPool jedisPool = new JedisPool(config,
                redisProperties.getHost(),
                redisProperties.getPort(),
                redisProperties.getTimeout().getNano(),
                redisProperties.getPassword(),
                databaseNum);
        return jedisPool;
    }

    /**
     * 从spring上下文获取redis配置
     *
     * @return
     */
    private static RedisProperties getRedisProperties() {
        RedisProperties redisProperties = SpringUtil.getBean(RedisProperties.class);
        return redisProperties;
    }

}
