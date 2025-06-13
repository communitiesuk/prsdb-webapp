package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!web-server-deactivated")
@Configuration
annotation class WebConfiguration(
    vararg val value: String,
)
