package uk.gov.communities.prsdb.webapp.config

import org.ff4j.FF4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.communities.prsdb.webapp.constants.EXAMPLE_FEATURE_FLAG_ONE

@Configuration
class FF4JConfig {
    @Bean
    fun ff4j(): FF4j {
        val ff4j = FF4j()

        ff4j.createFeature(EXAMPLE_FEATURE_FLAG_ONE, true)

        return ff4j
    }
}
