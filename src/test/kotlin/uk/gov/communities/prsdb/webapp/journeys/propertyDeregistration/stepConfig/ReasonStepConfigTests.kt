package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ReasonStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockPropertyDeregistrationService: PropertyDeregistrationService

    @Mock
    lateinit var mockConfirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>

    @Mock
    lateinit var mockState: PropertyDeregistrationJourneyState

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ReasonStep.ROUTE_SEGMENT)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(ReasonStep.ROUTE_SEGMENT)).thenReturn(mapOf("reason" to "Test reason"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(Complete.COMPLETE, result)
    }

    private fun setupStepConfig(): ReasonStepConfig {
        val stepConfig =
            ReasonStepConfig(
                mockPropertyOwnershipService,
                mockPropertyDeregistrationService,
                mockConfirmationEmailSender,
            )
        stepConfig.routeSegment = ReasonStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
