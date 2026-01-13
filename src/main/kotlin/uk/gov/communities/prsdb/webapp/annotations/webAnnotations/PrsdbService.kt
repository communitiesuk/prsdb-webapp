package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service

@Service
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PrsdbService(
    @get:AliasFor(annotation = Service::class) val value: String = "",
)
