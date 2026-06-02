package uk.gov.communities.prsdb.webapp.scheduledTaskRunners

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.application.DeleteIncompletePropertiesTaskApplicationRunner
import uk.gov.communities.prsdb.webapp.application.DeleteIncompletePropertiesTaskApplicationRunner.Companion.DELETE_INCOMPLETE_PROPERTIES_TASK_METHOD_NAME
import uk.gov.communities.prsdb.webapp.services.IncompletePropertiesService

@ExtendWith(MockitoExtension::class)
class DeleteIncompletePropertiesTaskApplicationRunnerTests {
    @Mock
    private lateinit var context: ApplicationContext

    @Mock
    private lateinit var incompletePropertiesService: IncompletePropertiesService

    @InjectMocks
    private lateinit var runner: DeleteIncompletePropertiesTaskApplicationRunner

    @Test
    fun `deleteIncompletePropertiesTaskLogic calls service to delete incomplete properties older than 28 days`() {
        // Arrange
        val method =
            DeleteIncompletePropertiesTaskApplicationRunner::class.java
                .getDeclaredMethod(DELETE_INCOMPLETE_PROPERTIES_TASK_METHOD_NAME)
        method.isAccessible = true

        // Act
        method.invoke(runner)

        // Assert
        verify(incompletePropertiesService).deleteIncompletePropertiesOlderThan28Days()
    }
}
