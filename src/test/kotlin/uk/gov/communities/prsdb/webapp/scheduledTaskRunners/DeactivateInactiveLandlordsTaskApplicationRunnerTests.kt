package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.DeactivateInactiveLandlordsTaskLogic
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.services.LandlordDeactivationService

@ExtendWith(MockitoExtension::class)
class DeactivateInactiveLandlordsTaskApplicationRunnerTests {
    @Mock
    private lateinit var landlordDeactivationService: LandlordDeactivationService

    @Mock
    private lateinit var featureFlagManager: FeatureFlagManager

    @InjectMocks
    private lateinit var taskLogic: DeactivateInactiveLandlordsTaskLogic

    // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
    @Test
    fun `deactivateInactiveLandlords does nothing and returns zero when the active inactive flag is off`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)

        val failureCount = taskLogic.deactivateInactiveLandlords()

        assertEquals(0, failureCount)
        verifyNoInteractions(landlordDeactivationService)
    }

    @Test
    fun `deactivateInactiveLandlords deactivates each candidate from both rules once and returns zero`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        whenever(landlordDeactivationService.getIdsOfLandlordsWhoNeverRegisteredAProperty()).thenReturn(listOf(1L, 2L))
        whenever(landlordDeactivationService.getIdsOfLandlordsWithNoActiveProperties()).thenReturn(listOf(2L, 3L))
        whenever(landlordDeactivationService.deactivateLandlordIfNoActiveProperties(1L)).thenReturn(true)
        whenever(landlordDeactivationService.deactivateLandlordIfNoActiveProperties(2L)).thenReturn(true)
        whenever(landlordDeactivationService.deactivateLandlordIfNoActiveProperties(3L)).thenReturn(false)

        val failureCount = taskLogic.deactivateInactiveLandlords()

        assertEquals(0, failureCount)
        verify(landlordDeactivationService, times(1)).deactivateLandlordIfNoActiveProperties(1L)
        verify(landlordDeactivationService, times(1)).deactivateLandlordIfNoActiveProperties(2L)
        verify(landlordDeactivationService, times(1)).deactivateLandlordIfNoActiveProperties(3L)
    }

    @Test
    fun `deactivateInactiveLandlords carries on after a failure and returns the number of failures`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        whenever(landlordDeactivationService.getIdsOfLandlordsWhoNeverRegisteredAProperty()).thenReturn(listOf(1L, 2L))
        whenever(landlordDeactivationService.getIdsOfLandlordsWithNoActiveProperties()).thenReturn(listOf(3L))
        whenever(landlordDeactivationService.deactivateLandlordIfNoActiveProperties(1L)).thenThrow(RuntimeException("DB error"))
        whenever(landlordDeactivationService.deactivateLandlordIfNoActiveProperties(2L)).thenReturn(true)
        whenever(landlordDeactivationService.deactivateLandlordIfNoActiveProperties(3L)).thenReturn(true)

        val failureCount = taskLogic.deactivateInactiveLandlords()

        assertEquals(1, failureCount)
        verify(landlordDeactivationService).deactivateLandlordIfNoActiveProperties(2L)
        verify(landlordDeactivationService).deactivateLandlordIfNoActiveProperties(3L)
    }
}
