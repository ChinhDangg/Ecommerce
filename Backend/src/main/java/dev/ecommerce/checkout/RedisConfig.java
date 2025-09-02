package dev.ecommerce.checkout;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory rf) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(rf);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory rf) {
        return new StringRedisTemplate(rf);
    }

    @Bean
    public RedisScript<List> reserveScript() {
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RESERVE_LUA);
        redisScript.setResultType(List.class); // return {status as int, remainingOrError)
        return redisScript;
    }

    @Bean
    public RedisScript<List> releaseScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(RELEASE_LUA);
        script.setResultType(List.class);
        return script;
    }

    private static final String RESERVE_LUA = """
            -- reserve_min.lua
            -- KEYS[1] = stock:{PID}
            -- KEYS[2] = holds:{PID}
            -- KEYS[3] = exp:{PID}
            -- ARGV[1] = CART
            -- ARGV[2] = requestedQty (absolute, >= 0)
            -- ARGV[3] = expiresAtMillis
            -- Returns: {1,newStock} on success; {0,errCode} on failure
            -- errCode: -1 not enough, -2 no cache, -3 bad request
            
            local stockKey, holdsKey, expKey = KEYS[1], KEYS[2], KEYS[3]
            local cart = ARGV[1]
            local req  = tonumber(ARGV[2])
            local exp  = tonumber(ARGV[3])
            
            if not cart or not req or req < 0 or not exp then return {0,-3} end
            
            local cur = tonumber(redis.call('GET', stockKey) or '-1')
            if cur == -1 then return {0,-2} end
            
            local prev = tonumber(redis.call('HGET', holdsKey, cart) or '0')
            local delta = req - prev
            
            if delta > 0 then
              if cur < delta then return {0,-1} end
              cur = redis.call('DECRBY', stockKey, delta)
            elseif delta < 0 then
              cur = redis.call('INCRBY', stockKey, -delta)
            end
            
            -- set/refresh reservation + expiry index
            if req == 0 then
              redis.call('HDEL', holdsKey, cart)
              redis.call('ZREM', expKey, cart)
            else
              redis.call('HSET', holdsKey, cart, req)
              redis.call('ZADD', expKey, exp, cart)
            end
            
            return {1, cur}
            
            """;

    private static final String RELEASE_LUA = """
            -- reaper_min.lua
            -- KEYS[1] = stock:{PID}
            -- KEYS[2] = holds:{PID}
            -- KEYS[3] = exp:{PID}
            -- ARGV[1] = nowMillis
            -- ARGV[2] = maxCount
            -- Returns: {processedCount, reclaimedTotal}
            
            local stockKey, holdsKey, expKey = KEYS[1], KEYS[2], KEYS[3]
            local now  = tonumber(ARGV[1])
            local maxn = tonumber(ARGV[2]) or 200
            
            local expired = redis.call('ZRANGEBYSCORE', expKey, '-inf', now, 'LIMIT', 0, maxn)
            local reclaimed, processed = 0, 0
            
            for _, cart in ipairs(expired) do
              local qty = tonumber(redis.call('HGET', holdsKey, cart) or '0')
              if qty > 0 then
                redis.call('INCRBY', stockKey, qty)
                reclaimed = reclaimed + qty
              end
              redis.call('HDEL', holdsKey, cart)
              redis.call('ZREM', expKey, cart)
              processed = processed + 1
            end
            
            return {processed, reclaimed}
            
            """;
}
