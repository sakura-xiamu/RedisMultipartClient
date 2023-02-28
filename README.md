# RedisMultipartClient

redis multipart client

## Redis多库实现

传统的RedisTemplate访问不了多个Redis的库号

使用通过指定库号进行Jedis实例化

@Test

    public void testRedisMultipartClient() {
    
        Jedis redisClient = RedisMultipartClient.getRedisClient(10);
        
        redisClient.set("yhk", "111");
        
        Jedis redisClient2 = RedisMultipartClient.getRedisClient(11);
        
        redisClient2.set("yhk", "111");
        
    }
