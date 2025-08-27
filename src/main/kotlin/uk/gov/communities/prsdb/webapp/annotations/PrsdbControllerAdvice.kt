package uk.gov.communities.prsdb.webapp.annotations

import org.springframework.context.annotation.Conditional
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
@Conditional(WebServerOnly::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbControllerAdvice
