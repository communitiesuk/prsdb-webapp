package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RestController

@Profile("!web-server-deactivated")
@RestController
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbRestController(
    @get:AliasFor(annotation = Controller::class) val value: String = "",
)
