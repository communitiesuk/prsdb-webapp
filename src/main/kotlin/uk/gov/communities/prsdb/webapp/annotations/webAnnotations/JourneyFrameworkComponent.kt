package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.context.annotation.Scope
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Scope("prototype")
@Service
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class JourneyFrameworkComponent(
    @get:AliasFor(annotation = Component::class) val value: String = "",
)
