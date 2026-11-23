package uk.gov.communities.prsdb.webapp.database.audit

import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.TaskName

@PrsdbTaskService
class AuditTaskNameRecorder : ApplicationListener<ApplicationStartedEvent> {
    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        val context = event.applicationContext
        AuditRevisionListener.currentTaskName =
            context
                .getBeanNamesForAnnotation(TaskName::class.java)
                .mapNotNull { context.findAnnotationOnBean(it, TaskName::class.java)?.value?.ifBlank { null } }
                .singleOrNull()
    }
}
