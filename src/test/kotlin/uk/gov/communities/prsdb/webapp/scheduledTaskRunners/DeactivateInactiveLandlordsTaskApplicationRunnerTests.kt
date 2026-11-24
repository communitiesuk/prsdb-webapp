package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.DeactivateInactiveLandlordsTaskLogic
import uk.gov.communities.prsdb.webapp.services.DeactivationService

@ExtendWith(MockitoExtension::class)
class DeactivateInactiveLandlordsTaskApplicationRunnerTests {
    @Mock
    private lateinit var deactivationService: DeactivationService

    @InjectMocks
    private lateinit var taskLogic: DeactivateInactiveLandlordsTaskLogic

    @Test
    fun `deactivateInactiveLandlords deactivates each candidate from both rules once and returns zero`() {
        whenever(
            deactivationService.getIdsOfLandlordsCreatedBeforeDeactivationCutoffWhoNeverRegisteredAProperty(),
        ).thenReturn(listOf(1L, 2L))
        whenever(
            deactivationService.getIdsOfLandlordsWhoPreviouslyRegisteredAPropertyButHaveNoActiveProperties(),
        ).thenReturn(listOf(2L, 3L))
        whenever(deactivationService.deactivateLandlordIfNoActiveProperties(1L)).thenReturn(true)
        whenever(deactivationService.deactivateLandlordIfNoActiveProperties(2L)).thenReturn(true)
        whenever(deactivationService.deactivateLandlordIfNoActiveProperties(3L)).thenReturn(false)

        val failureCount = taskLogic.deactivateInactiveLandlords()

        assertEquals(0, failureCount)
        verify(deactivationService, times(1)).deactivateLandlordIfNoActiveProperties(1L)
        verify(deactivationService, times(1)).deactivateLandlordIfNoActiveProperties(2L)
        verify(deactivationService, times(1)).deactivateLandlordIfNoActiveProperties(3L)
    }

    @Test
    fun `deactivateInactiveLandlords carries on after a failure and returns the number of failures`() {
        whenever(
            deactivationService.getIdsOfLandlordsCreatedBeforeDeactivationCutoffWhoNeverRegisteredAProperty(),
        ).thenReturn(listOf(1L, 2L))
        whenever(deactivationService.getIdsOfLandlordsWhoPreviouslyRegisteredAPropertyButHaveNoActiveProperties()).thenReturn(listOf(3L))
        whenever(deactivationService.deactivateLandlordIfNoActiveProperties(1L)).thenThrow(RuntimeException("DB error"))
        whenever(deactivationService.deactivateLandlordIfNoActiveProperties(2L)).thenReturn(true)
        whenever(deactivationService.deactivateLandlordIfNoActiveProperties(3L)).thenReturn(true)

        val failureCount = taskLogic.deactivateInactiveLandlords()

        assertEquals(1, failureCount)
        verify(deactivationService).deactivateLandlordIfNoActiveProperties(2L)
        verify(deactivationService).deactivateLandlordIfNoActiveProperties(3L)
    }
}
