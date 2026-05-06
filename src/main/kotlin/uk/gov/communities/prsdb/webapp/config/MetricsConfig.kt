package uk.gov.communities.prsdb.webapp.config

import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.context.annotation.Bean
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebConfiguration

@PrsdbWebConfiguration
class MetricsConfig {
    @Bean
    fun denyHttpServerRequestMetrics(): MeterFilter = MeterFilter.denyNameStartsWith("http.server.requests")
}
