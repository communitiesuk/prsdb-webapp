package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator

class TaskRouteRedirectStepTests {
    @Test
    fun `config mode always returns REDIRECT`() {
        val config = TaskRouteRedirectStepConfig()
        assertEquals(TaskRouteRedirectMode.REDIRECT, config.mode(mock()))
    }

    @Test
    fun `step uses a redirecting lifecycle orchestrator`() {
        val step = TaskRouteRedirectStep(TaskRouteRedirectStepConfig())
        assertTrue(step.lifecycleOrchestrator is RedirectingStepLifecycleOrchestrator)
    }
}
