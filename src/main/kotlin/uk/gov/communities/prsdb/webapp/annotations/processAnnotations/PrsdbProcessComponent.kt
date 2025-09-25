package uk.gov.communities.prsdb.webapp.annotations.processAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Component
@Conditional(ProcessOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbProcessComponent(
    @get:AliasFor(annotation = Component::class) val value: String = "",
)
