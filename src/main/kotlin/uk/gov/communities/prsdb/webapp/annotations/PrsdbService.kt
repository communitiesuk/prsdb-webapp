package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Service

@Profile("!web-server-deactivated")
@Service
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PrsdbService(
    @get:AliasFor(annotation = Service::class) val value: String = "",
)
