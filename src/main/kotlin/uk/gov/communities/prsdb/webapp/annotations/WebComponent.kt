package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Profile("!web-server-deactivated")
@Component
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class WebComponent(
    @get:AliasFor(annotation = Component::class) val value: String = "",
)
