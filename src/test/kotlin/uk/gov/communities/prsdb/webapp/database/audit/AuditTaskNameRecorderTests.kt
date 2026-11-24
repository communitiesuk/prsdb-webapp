package uk.gov.communities.prsdb.webapp.database.audit

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import uk.gov.communities.prsdb.webapp.application.DefaultScheduledTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.DeleteExpiredJointLandlordInvitationsTaskApplicationRunner
import java.time.Duration

class AuditTaskNameRecorderTests {
    @AfterEach
    fun tearDown() {
        AuditRevisionListener.currentTaskName = null
    }

    @Test
    fun `onApplicationEvent sets the name of the running task on the audit revision listener`() {
        recordTaskNameWithActiveProfiles("web-server-deactivated", "scheduled-task", "jl-invitation-deletion-scheduled-task")

        assertEquals("jl-invitation-deletion-scheduled-task", AuditRevisionListener.currentTaskName)
    }

    @Test
    fun `onApplicationEvent sets no task name when no named task is running`() {
        AuditRevisionListener.currentTaskName = "stale-task"

        recordTaskNameWithActiveProfiles("web-server-deactivated", "scheduled-task")

        assertNull(AuditRevisionListener.currentTaskName)
    }

    private fun recordTaskNameWithActiveProfiles(vararg profiles: String) {
        AnnotationConfigApplicationContext().use { context ->
            context.environment.setActiveProfiles(*profiles)
            listOf(
                DeleteExpiredJointLandlordInvitationsTaskApplicationRunner::class.java,
                DefaultScheduledTaskApplicationRunner::class.java,
            ).forEach { context.registerBean(it, { definition -> definition.isLazyInit = true }) }
            context.refresh()

            AuditTaskNameRecorder().onApplicationEvent(ApplicationStartedEvent(SpringApplication(), arrayOf(), context, Duration.ZERO))
        }
    }
}
