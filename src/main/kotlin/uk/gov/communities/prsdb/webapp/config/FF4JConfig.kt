package uk.gov.communities.prsdb.webapp.config

import org.ff4j.FF4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FF4JConfig {
    @Bean
    fun ff4j() = FF4j()
}
