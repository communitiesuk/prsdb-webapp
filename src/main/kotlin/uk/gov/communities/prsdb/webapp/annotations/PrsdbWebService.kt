package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service

@Conditional(WebServerOnly::class)
@Service
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PrsdbWebService(
    @get:AliasFor(annotation = Service::class) val value: String = "",
)
