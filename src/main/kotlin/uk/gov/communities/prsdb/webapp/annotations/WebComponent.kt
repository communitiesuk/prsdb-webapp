package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("!web-server-deactivated")
@Component
annotation class WebComponent(
    vararg val value: String,
)
