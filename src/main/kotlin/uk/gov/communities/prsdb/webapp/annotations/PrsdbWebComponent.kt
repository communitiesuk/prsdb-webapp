package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Conditional(WebServerOnly::class)
@Component
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PrsdbWebComponent(
    @get:AliasFor(annotation = Component::class) val value: String = "",
)
