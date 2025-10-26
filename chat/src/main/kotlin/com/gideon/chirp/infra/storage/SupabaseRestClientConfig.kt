package com.gideon.chirp.infra.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class SupabaseRestClientConfig(
    @param:Value("\${supabase.url}") private val supabseUrl: String,
    @param:Value("\${supabase.service-key}") private val supabseServiceKey: String,
) {

    @Bean
    fun supabaseRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(supabseUrl)
            .defaultHeader("Authorization", "Bearer $supabseServiceKey")
            .build()
    }
}