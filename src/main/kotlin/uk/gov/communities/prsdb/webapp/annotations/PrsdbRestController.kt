package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RestController

@RestController
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(WebServerOnly::class)
annotation class PrsdbRestController(
    @get:AliasFor(annotation = Controller::class) val value: String = "",
)
