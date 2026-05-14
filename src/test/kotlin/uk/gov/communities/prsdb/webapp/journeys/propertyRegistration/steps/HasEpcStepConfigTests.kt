package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasEpcStepConfigTests {
    @Mock
    lateinit var mockJourneyState: EpcState

    val routeSegment = HasEpcStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when hasCert is null and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasCert" to null, "action" to CONTINUE_BUTTON_ACTION_NAME))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_EPC when hasCert is true and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasCert" to true, "action" to CONTINUE_BUTTON_ACTION_NAME))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasEpcMode.HAS_EPC, result)
    }

    @Test
    fun `mode returns NO_EPC when hasCert is false and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasCert" to false, "action" to CONTINUE_BUTTON_ACTION_NAME))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasEpcMode.NO_EPC, result)
    }

    @Test
    fun `mode returns PROVIDE_LATER when action is provideThisLater and route is allowed`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(true)
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to null, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasEpcMode.PROVIDE_LATER, result)
    }

    @Test
    fun `mode returns PROVIDE_LATER regardless of hasCert value when action is provideThisLater and route is allowed`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(true)
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to true, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasEpcMode.PROVIDE_LATER, result)
    }

    @Test
    fun `mode throws UnrecoverableJourneyStateException when action is provideThisLater but route is not allowed`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(false)
        whenever(mockJourneyState.journeyId).thenReturn("test-journey-id")
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to true, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act, assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockJourneyState) }
    }

    private fun setupStepConfig(): HasEpcStepConfig {
        val stepConfig = HasEpcStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
