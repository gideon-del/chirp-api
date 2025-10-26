package com.gideon.chirp.infra.rate_limiting

import com.gideon.chirp.domain.exception.RateLimitException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class IpRateLimiter(
    private  val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val IP_RATE_LIMIT_PREFIX ="rate_limit:ip"

    }
    @Value("classpath:ip_rate_limit.lua")
    private lateinit var  rateLimitRescours: Resource
    private  val logger = LoggerFactory.getLogger(IpRateLimiter::class.java)

    private val rateLimitScript by lazy {
        val script = rateLimitRescours.inputStream.use{
            it.readBytes().decodeToString()
        }

        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }

    fun <T> withIpRateLimit(
        ipAddress: String,
        resetsIn: Duration,
        maxRequestPerIp: Int,
        action: () -> T
    ): T  {

        val key = "$IP_RATE_LIMIT_PREFIX:$ipAddress"

      val result = redisTemplate.execute(rateLimitScript, listOf(key),
           maxRequestPerIp.toString(),
         resetsIn.seconds.toString()
         )
     val currentCount = result[0].toInt()

     return if(currentCount <= maxRequestPerIp) {
         action()
        } else {
            val ttl = result[1]

            throw RateLimitException(ttl)
        }
    }
}