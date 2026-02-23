package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class SelectAddressStepConfigTests {
    @Mock
    lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    lateinit var mockAddressState: AddressState

    val routeSegment = SelectAddressStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when step data is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockAddressState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockAddressState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when selectedOption is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockAddressState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockAddressState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns MANUAL_ADDRESS when manual address chosen`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockAddressState.getStepData(routeSegment)).thenReturn(mapOf("selectedOption" to MANUAL_ADDRESS_CHOSEN))

        // Act
        val result = stepConfig.mode(mockAddressState)

        // Assert
        assertEquals(SelectAddressMode.MANUAL_ADDRESS, result)
    }

    @Test
    fun `mode returns ADDRESS_ALREADY_REGISTERED when address is already registered`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockAddressState.getStepData(routeSegment)).thenReturn(mapOf("selectedOption" to "1"))
        whenever(mockAddressState.isAddressAlreadyRegistered).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockAddressState)

        // Assert
        assertEquals(SelectAddressMode.ADDRESS_ALREADY_REGISTERED, result)
    }

    @Test
    fun `mode returns ADDRESS_SELECTED when valid address selected`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockAddressState.getStepData(routeSegment)).thenReturn(mapOf("selectedOption" to "1"))
        whenever(mockAddressState.isAddressAlreadyRegistered).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockAddressState)

        // Assert
        assertEquals(SelectAddressMode.ADDRESS_SELECTED, result)
    }

    private fun setupStepConfig(): SelectAddressStepConfig {
        val stepConfig = SelectAddressStepConfig(mockAddressAvailabilityService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
