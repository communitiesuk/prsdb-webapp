package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AliasFor

@Profile("!web-server-deactivated")
@Configuration
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PrsdbWebConfiguration(
    @get:AliasFor(annotation = Configuration::class) val value: String = "",
)
