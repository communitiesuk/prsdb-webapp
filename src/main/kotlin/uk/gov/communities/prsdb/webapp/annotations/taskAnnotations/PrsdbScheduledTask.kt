package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.springframework.context.annotation.Conditional
import org.springframework.core.annotation.AliasFor
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@TaskName
@Conditional(ScheduledTaskHasName::class)
@Order
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrsdbScheduledTask(
    @get:AliasFor(annotation = TaskName::class, attribute = "value") val name: String = "",
    @get:AliasFor(annotation = Order::class, attribute = "value") val precedence: Int = 1,
)
