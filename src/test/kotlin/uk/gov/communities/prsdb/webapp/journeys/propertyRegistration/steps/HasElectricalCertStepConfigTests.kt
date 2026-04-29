package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.NullSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasElectricalCertStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    val routeSegment = HasElectricalCertStep.ROUTE_SEGMENT

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
    fun `mode returns null when electricalCertType is null and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to null, "action" to CONTINUE_BUTTON_ACTION_NAME))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_EIC when electricalCertType is HAS_EIC and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to HasElectricalSafetyCertificate.HAS_EIC, "action" to CONTINUE_BUTTON_ACTION_NAME))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(HasElectricalCertMode.HAS_EIC, result)
    }

    @Test
    fun `mode returns HAS_EICR when electricalCertType is HAS_EICR and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to HasElectricalSafetyCertificate.HAS_EICR, "action" to CONTINUE_BUTTON_ACTION_NAME))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(HasElectricalCertMode.HAS_EICR, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when electricalCertType is NO_CERTIFICATE and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(
                mapOf("electricalCertType" to HasElectricalSafetyCertificate.NO_CERTIFICATE, "action" to CONTINUE_BUTTON_ACTION_NAME),
            )

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(HasElectricalCertMode.NO_CERTIFICATE, result)
    }

    @ParameterizedTest
    @NullSource
    @EnumSource(HasElectricalSafetyCertificate::class)
    fun `mode returns PROVIDE_THIS_LATER when action is provideThisLater and route is allowed`(
        electricalCertType: HasElectricalSafetyCertificate?,
    ) {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.allowProvideCertificateLaterRoute).thenReturn(true)
        whenever(mockState.getStepData(routeSegment)).thenReturn(
            mapOf("electricalCertType" to electricalCertType, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(HasElectricalCertMode.PROVIDE_THIS_LATER, result)
    }

    @Test
    fun `mode throws an error when action is provideThisLater but route is not allowed`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.allowProvideCertificateLaterRoute).thenReturn(false)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.getStepData(routeSegment)).thenReturn(
            mapOf("electricalCertType" to HasElectricalSafetyCertificate.HAS_EIC, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act, assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockState) }
    }

    private fun setupStepConfig(): HasElectricalCertStepConfig {
        val stepConfig = HasElectricalCertStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
