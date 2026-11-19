package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.DeactivateMissingDetailPropertiesTaskLogic
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService

@ExtendWith(MockitoExtension::class)
class DeactivateMissingDetailPropertiesTaskApplicationRunnerTests {
    @Mock
    private lateinit var featureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var activeInactiveService: ActiveInactiveService

    @InjectMocks
    private lateinit var taskLogic: DeactivateMissingDetailPropertiesTaskLogic

    @Test
    fun `returns no failures without calling the service when active inactive feature is disabled`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)

        val failureCount = taskLogic.deactivateMissingDetailProperties()

        assertEquals(0, failureCount)
        verifyNoInteractions(activeInactiveService)
    }

    @Test
    fun `returns the service failure count when active inactive feature is enabled`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        whenever(activeInactiveService.deactivatePropertiesWithOverdueTasksReturningFailureCount()).thenReturn(2)

        val failureCount = taskLogic.deactivateMissingDetailProperties()

        assertEquals(2, failureCount)
        verify(activeInactiveService).deactivatePropertiesWithOverdueTasksReturningFailureCount()
    }

    @Test
    fun `returns zero when the service reports no failed deactivations`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        whenever(activeInactiveService.deactivatePropertiesWithOverdueTasksReturningFailureCount()).thenReturn(0)

        val failureCount = taskLogic.deactivateMissingDetailProperties()

        assertEquals(0, failureCount)
        verify(activeInactiveService).deactivatePropertiesWithOverdueTasksReturningFailureCount()
    }
}
