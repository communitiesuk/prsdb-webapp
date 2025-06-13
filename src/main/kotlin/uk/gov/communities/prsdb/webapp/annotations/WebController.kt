package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller

@Profile("!web-server-deactivated")
@Controller
annotation class WebController(
    vararg val value: String,
)
// QQ - RestController is not overridden by this annotation - check for other classes
