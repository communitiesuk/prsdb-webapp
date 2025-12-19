package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.springframework.context.annotation.Scope
import org.springframework.core.annotation.AliasFor

@Scope("prototype")
@PrsdbWebComponent
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class JourneyFrameworkComponent(
    @get:AliasFor(annotation = PrsdbWebComponent::class) val value: String = "",
)
