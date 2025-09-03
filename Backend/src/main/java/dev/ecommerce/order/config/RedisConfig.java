package dev.ecommerce.order.config;

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

    @Bean
    public RedisScript<Integer> removeReserveScript() {
        DefaultRedisScript<Integer> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(REMOVE_LUA);
        redisScript.setResultType(Integer.class);
        return redisScript;
    }

    private static final String RESERVE_LUA = """
            -- reserve_global.lua
            -- KEYS[1] = stock:{pid}    -- the current stock of the product
            -- KEYS[2] = holds:{pid}    -- cart that hold number of stock
            -- KEYS[3] = exp:{pid}      -- expiry time as score for cart holding the product id
            -- KEYS[4] = exp:all               -- global ZSET to help retrieving all expiry entry
            -- ARGV[1] = cart
            -- ARGV[2] = requestedQty (absolute)
            -- ARGV[3] = expiresAtMillis
            -- ARGV[4] = pid
            -- Returns: {1,newStock} on success; {0,errCode} on failure
            -- errCode: -1 not enough, -2 no cache, -3 bad request
            
            local stockKey, holdsKey, expKey, expAll = KEYS[1], KEYS[2], KEYS[3], KEYS[4]
            local cart, req, exp, pid = ARGV[1], tonumber(ARGV[2]), tonumber(ARGV[3]), ARGV[4]
            
            if not cart or not req or req < 0 or not exp then
              return {0, -3}
            end
            
            -- current stock
            local cur = tonumber(redis.call('GET', stockKey) or '-1')
            if cur == -1 then
              return {0, -2}
            end
            
            -- previous reservation for this cart
            local prev = tonumber(redis.call('HGET', holdsKey, cart) or '0')
            local delta = req - prev
            
            -- adjust stock if needed
            if delta > 0 then
              if cur < delta then return {0, -1} end
              cur = redis.call('DECRBY', stockKey, delta)
            elseif delta < 0 then
              cur = redis.call('INCRBY', stockKey, -delta)
            end
            
            local globalMember = pid .. ":" .. cart
            
            -- update reservation
            if req == 0 then
              -- releasing reservation
              redis.call('HDEL', holdsKey, cart)
              redis.call('ZREM', expKey, cart)
              redis.call('ZREM', expAll, globalMember)
            else
              -- new or updated reservation
              redis.call('HSET', holdsKey, cart, req)
              redis.call('ZADD', expKey, exp, cart)
              redis.call('ZADD', expAll, exp, globalMember)
            end
            
            return {1, cur}
            """;

    private static final String RELEASE_LUA = """
            -- reaper_global.lua
            -- KEYS[1] = exp:all
            -- ARGV[1] = nowMillis
            -- ARGV[2] = maxCount
            -- Returns: {processed, reclaimed}
            
            local allKey = KEYS[1]
            local now  = tonumber(ARGV[1])
            local maxn = tonumber(ARGV[2]) or 200
            
            local members = redis.call('ZRANGEBYSCORE', allKey, '-inf', now, 'LIMIT', 0, maxn)
            local processed, reclaimed = 0, 0
            
            for _, m in ipairs(members) do
              local sep = string.find(m, ":")
              if sep then
                local pid  = string.sub(m, 1, sep-1)
                local cart = string.sub(m, sep+1)
            
                local holdsKey = "holds:{" .. pid .. "}"
                local expKey   = "exp:{" .. pid .. "}"
                local stockKey = "stock:{" .. pid .. "}"
            
                local qty = tonumber(redis.call('HGET', holdsKey, cart) or '0')
                if qty > 0 then
                  redis.call('INCRBY', stockKey, qty)
                  reclaimed = reclaimed + qty
                end
                redis.call('HDEL', holdsKey, cart)
                redis.call('ZREM', expKey, cart)
                redis.call('ZREM', allKey, m)
                processed = processed + 1
              else
                redis.call('ZREM', allKey, m) -- malformed member
              end
            end
            
            return {processed, reclaimed}
            """;

    // remove reserved
    private static final String REMOVE_LUA = """
            -- KEYS[1] = holds:{pid}
            -- KEYS[2] = exp:{pid}
            -- KEYS[3] = exp:all
            -- ARGV[1] = cartId
            -- ARGV[2] = pid
            local holdsKey, expKey, allKey = KEYS[1], KEYS[2], KEYS[3]
            local cart, pid = ARGV[1], ARGV[2]
            redis.call('HDEL', holdsKey, cart)
            redis.call('ZREM', expKey, cart)
            redis.call('ZREM', allKey, pid .. ":" .. cart)
            return 1
            """;
}
