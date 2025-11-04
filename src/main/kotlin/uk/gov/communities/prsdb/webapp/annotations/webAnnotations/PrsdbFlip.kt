package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.ff4j.aop.Flip
import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor

@Flip(name = "")
@Conditional(WebServerOnly::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class PrsdbFlip(
    @get:AliasFor(annotation = Flip::class) val name: String,
    @get:AliasFor(annotation = Flip::class) val alterBean: String = "",
)
