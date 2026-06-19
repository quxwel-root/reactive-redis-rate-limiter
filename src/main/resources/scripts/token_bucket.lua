local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local fill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local bucket = redis.call("HMGET", key, "tokens", "last_refreshed")
local tokens = tonumber(bucket[1])
local last_refreshed = tonumber(bucket[2])

if tokens == nil then
    tokens = capacity
    last_refreshed = now
end

local delta = math.max(0, now - last_refreshed)
local filled_tokens = math.min(capacity, tokens + (delta * fill_rate))
local allowed = filled_tokens >= requested

if allowed then
    filled_tokens = filled_tokens - requested
    redis.log(redis.LOG_WARNING, "DEBUG: HMSET " .. key .. " tokens " .. filled_tokens)

    local res = redis.call("HMSET", key, "tokens", filled_tokens, "last_refreshed", now)
    redis.call("EXPIRE", key, 300)
    return 1
else
    return 0
end