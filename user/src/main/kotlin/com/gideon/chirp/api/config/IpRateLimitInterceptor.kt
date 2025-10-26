package com.gideon.chirp.api.config

import com.gideon.chirp.domain.exception.RateLimitException
import com.gideon.chirp.infra.rate_limiting.IpRateLimiter
import com.gideon.chirp.infra.rate_limiting.IpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitInterceptor(
    private  val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${chirp.rate-limit.apply-limit}")
    private val applyLimit: Boolean
): HandlerInterceptor {
    private  val logger = LoggerFactory.getLogger(IpRateLimitInterceptor::class.java)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if(handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IpRateLimit::class.java)
            if(annotation != null) {
                val clientIp = ipResolver.getClientIp(request)
                return try {
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = clientIp,
                        resetsIn = Duration.of(
                        annotation.duration,
                        annotation.unit.toChronoUnit()
                    ),
                        maxRequestPerIp = annotation.request,
                        action = { true }
                        )
                }catch (e: RateLimitException) {

                    response.sendError(429)
                    false
                }

            }
        }
        return super.preHandle(request, response, handler)
    }
}