package com.gideon.chirp.infra.rate_limiting

import com.gideon.chirp.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address


@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {

    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16",
            "127.0.0.0/8",
            "::1/128",
            "fc00::/7",
            "fe80::/10"
        ).map {
            IpAddressMatcher(it)
        }

        private  val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private  val trustedMatchers: List<IpAddressMatcher> = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy
                proxy.contains(":") -> "$proxy/128"
                else -> "$proxy/32"
            }

            IpAddressMatcher(cidr)
        }
private  val logger = LoggerFactory.getLogger(IpResolver::class.java)
    fun getClientIp(req: HttpServletRequest): String {
       val remoteAddr = req.remoteAddr

        if(!isFromTrustedProxy(remoteAddr)){
            if(nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddr")

                throw SecurityException("No valid client IP in proxy header")
            }

            return remoteAddr
        }

val clientIp = extractFromXRealIp(req,remoteAddr)
        if(clientIp == null){
            logger.warn("No valid Ip in proxy headers")
            if(nginxConfig.requireProxy){
                throw SecurityException("No valid client IP in proxy headers")
            }
        }

        return clientIp ?: remoteAddr
    }

    private  fun extractFromXRealIp(req: HttpServletRequest,proxyIp: String): String? {
        return  req.getHeader("X-Real-IP")?.let { header ->
validateAndNormalizeIp(header, "X-Real-IP", proxyIp)
        }
    }
    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimedIp = ip.trim()
        if(trimedIp.isBlank() || INVALID_IPS.contains(trimedIp)){
            logger.debug("Invalid Up in $headerName: $ip from proxy $proxyIp")
            return null
        }

      return  try {
            val inetAddr = when {
                trimedIp.contains(":") -> Inet6Address.getByName(trimedIp)
                trimedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))  ->
                    Inet4Address.getByName(trimedIp)
                else -> {
                    logger.warn("Invalid Ip format in $headerName: $trimedIp from proxy $proxyIp ")
                    return null
                }
            }
          if(isPrivateIp(inetAddr.hostAddress)){
              logger.debug(("Private IP in $headerName: $trimedIp from proxy $proxyIp"))
              return null
          }
          return  inetAddr.hostAddress
        }catch (e: Exception) {
            logger.warn("Invalid Ip format in $headerName: $trimedIp from proxy $proxyIp ", e)
          null
        }
    }
    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip)}
    }
    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { it.matches(ip)}
    }
}