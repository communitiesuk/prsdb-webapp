package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor

@Configuration
@Conditional(TaskOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbTaskConfiguration(
    @get:AliasFor(annotation = Configuration::class) val value: String = "",
)
