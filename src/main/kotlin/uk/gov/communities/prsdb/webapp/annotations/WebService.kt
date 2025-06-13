package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!web-server-deactivated")
@Service
annotation class WebService(
    vararg val value: String,
)
