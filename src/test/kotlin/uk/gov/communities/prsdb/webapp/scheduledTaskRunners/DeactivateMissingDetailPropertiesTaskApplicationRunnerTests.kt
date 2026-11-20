package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.application.DeactivateMissingDetailPropertiesTaskLogic
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService

@ExtendWith(MockitoExtension::class)
class DeactivateMissingDetailPropertiesTaskApplicationRunnerTests {
    @Mock
    private lateinit var activeInactiveService: ActiveInactiveService

    @InjectMocks
    private lateinit var taskLogic: DeactivateMissingDetailPropertiesTaskLogic

    @Test
    fun `returns the service failure count`() {
        whenever(activeInactiveService.deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()).thenReturn(2)

        val failureCount = taskLogic.deactivateMissingDetailPropertiesReturningFailureCount()

        assertEquals(2, failureCount)
        verify(activeInactiveService).deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()
    }

    @Test
    fun `returns zero when the service reports no failed deactivations`() {
        whenever(activeInactiveService.deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()).thenReturn(0)

        val failureCount = taskLogic.deactivateMissingDetailPropertiesReturningFailureCount()

        assertEquals(0, failureCount)
        verify(activeInactiveService).deactivateMissingDetailPropertiesPastDueDateReturningFailureCount()
    }
}
