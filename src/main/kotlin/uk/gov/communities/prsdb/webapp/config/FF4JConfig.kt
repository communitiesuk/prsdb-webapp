package uk.gov.communities.prsdb.webapp.config

import org.ff4j.FF4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import uk.gov.communities.prsdb.webapp.constants.FIRST_TOY_FEATURE_FLAG

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true) // don't know if we need this
class FF4JConfig {
    @Bean
    fun ff4j(): FF4j {
        val ff4j = FF4j()

        ff4j.createFeature(FIRST_TOY_FEATURE_FLAG, true)

        return ff4j
    }
}
