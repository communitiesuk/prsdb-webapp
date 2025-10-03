package uk.gov.communities.prsdb.webapp.annotations.processAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service

@Service
@Conditional(ProcessOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbProcessService(
    @get:AliasFor(annotation = Service::class) val value: String = "",
)
