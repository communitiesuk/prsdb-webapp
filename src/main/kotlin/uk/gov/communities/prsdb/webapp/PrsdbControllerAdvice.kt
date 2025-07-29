package uk.gov.communities.prsdb.webapp

import org.springframework.context.annotation.Conditional
import org.springframework.web.bind.annotation.ControllerAdvice
import uk.gov.communities.prsdb.webapp.annotations.WebServerOnly

@ControllerAdvice
@Conditional(WebServerOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbControllerAdvice
