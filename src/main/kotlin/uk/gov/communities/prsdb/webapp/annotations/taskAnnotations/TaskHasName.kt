package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

open class TaskHasName : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean {
        val activeProfiles = context.environment.activeProfiles
        val taskName = metadata.getAnnotationAttributes(TaskName::class.java.name)!!["value"].toString()
        return activeProfiles.contains("web-server-deactivated") && (activeProfiles.contains(taskName) || taskName.isBlank())
    }
}

class ScheduledTaskHasName : TaskHasName() {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean = super.matches(context, metadata) && context.environment.activeProfiles.contains("scheduled-task")
}
