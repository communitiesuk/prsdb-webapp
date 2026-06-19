package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.application.DeleteIncompletePropertiesTaskLogic
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService

@ExtendWith(MockitoExtension::class)
class DeleteIncompletePropertiesTaskApplicationRunnerTests {
    @Mock
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @InjectMocks
    private lateinit var taskLogic: DeleteIncompletePropertiesTaskLogic

    @Test
    fun `deleteIncompleteProperties calls service to delete incomplete properties older than 28 days`() {
        // Act
        taskLogic.deleteIncompleteProperties()

        // Assert
        verify(incompletePropertiesService).deleteIncompletePropertiesOlderThan28Days()
    }
}
