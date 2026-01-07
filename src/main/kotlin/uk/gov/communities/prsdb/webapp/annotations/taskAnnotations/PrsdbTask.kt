package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Component
@TaskName
@Conditional(TaskHasName::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbTask(
    @get:AliasFor(annotation = TaskName::class, attribute = "value") val name: String = "",
    @get:AliasFor(annotation = Component::class, attribute = "value") val beanName: String = "",
)
