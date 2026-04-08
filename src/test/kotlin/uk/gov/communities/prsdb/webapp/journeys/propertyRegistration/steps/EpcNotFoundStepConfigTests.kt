package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FindEpcByCertificateNumberFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EpcNotFoundStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    @Mock
    lateinit var mockFindYourEpcStep: FindYourEpcStep

    private val routeSegment = EpcNotFoundStep.ROUTE_SEGMENT

    private fun setupStepConfig(): EpcNotFoundStepConfig {
        val stepConfig = EpcNotFoundStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `getStepSpecificContent returns certificateNumber from findYourEpcStep form model`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val expectedCertificateNumber = "0000-1111-2222-3333-4444"
        val formModel =
            FindEpcByCertificateNumberFormModel().apply {
                certificateNumber = expectedCertificateNumber
            }
        whenever(mockState.findYourEpcStep).thenReturn(mockFindYourEpcStep)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockFindYourEpcStep.formModelOrNull).thenReturn(formModel)
        whenever(mockFindYourEpcStep.isStepReachable).thenReturn(true)
        whenever(mockFindYourEpcStep.routeSegment).thenReturn(FindYourEpcStep.ROUTE_SEGMENT)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(expectedCertificateNumber, result["certificateNumber"])
    }

    @Test
    fun `getStepSpecificContent returns null certificateNumber when findYourEpcStep form model is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.findYourEpcStep).thenReturn(mockFindYourEpcStep)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockFindYourEpcStep.formModelOrNull).thenReturn(null)
        whenever(mockFindYourEpcStep.isStepReachable).thenReturn(true)
        whenever(mockFindYourEpcStep.routeSegment).thenReturn(FindYourEpcStep.ROUTE_SEGMENT)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertNull(result["certificateNumber"])
    }

    @Test
    fun `getStepSpecificContent returns searchAgainUrl pointing to findYourEpcStep with journeyId`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val journeyId = "test-journey-id"
        whenever(mockState.findYourEpcStep).thenReturn(mockFindYourEpcStep)
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockFindYourEpcStep.formModelOrNull).thenReturn(null)
        whenever(mockFindYourEpcStep.isStepReachable).thenReturn(true)
        whenever(mockFindYourEpcStep.routeSegment).thenReturn(FindYourEpcStep.ROUTE_SEGMENT)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals("${FindYourEpcStep.ROUTE_SEGMENT}?journeyId=$journeyId", result["searchAgainUrl"])
    }

    @Test
    fun `getStepSpecificContent returns null searchAgainUrl when findYourEpcStep is not reachable`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.findYourEpcStep).thenReturn(mockFindYourEpcStep)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockFindYourEpcStep.formModelOrNull).thenReturn(null)
        whenever(mockFindYourEpcStep.isStepReachable).thenReturn(false)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertNull(result["searchAgainUrl"])
    }
}
