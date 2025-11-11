package uk.gov.communities.prsdb.webapp.annotations.processAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor

@Configuration
@Conditional(ProcessOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbProcessConfiguration(
    @get:AliasFor(annotation = Configuration::class) val value: String = "",
)
