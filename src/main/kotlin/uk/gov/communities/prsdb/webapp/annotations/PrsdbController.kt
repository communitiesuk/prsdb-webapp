package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Controller

@Profile("!web-server-deactivated")
@Controller
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbController(
    @get:AliasFor(annotation = Controller::class) val value: String = "",
)
