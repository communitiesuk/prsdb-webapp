package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

import org.ff4j.aop.Flip
import org.springframework.core.annotation.AliasFor

@Flip(name = "")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class PrsdbFlip(
    @get:AliasFor(annotation = Flip::class) val name: String,
    @get:AliasFor(annotation = Flip::class) val alterBean: String = "",
)
