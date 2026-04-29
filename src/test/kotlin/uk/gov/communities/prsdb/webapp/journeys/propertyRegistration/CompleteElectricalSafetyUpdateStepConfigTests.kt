package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.CompleteElectricalSafetyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.UpdateElectricalSafetyJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@ExtendWith(MockitoExtension::class)
class CompleteElectricalSafetyUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockState: UpdateElectricalSafetyJourneyState

    private lateinit var stepConfig: CompleteElectricalSafetyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteElectricalSafetyUpdateStepConfig(mockPropertyComplianceService)
    }

    @Test
    fun `afterStepIsReached calls updateElectricalSafety with expiry date and upload ids`() {
        val expiryDate = LocalDate(2026, 6, 15)
        val uploadIds = listOf(1L, 2L)

        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(expiryDate)
        whenever(mockState.electricalUploadIds).thenReturn(uploadIds)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyComplianceService).updateElectricalSafety(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            electricalSafetyExpiryDate = expiryDate.toJavaLocalDate(),
            electricalSafetyCertUploadIds = uploadIds,
        )
    }

    @Test
    fun `afterStepIsReached calls updateElectricalSafety with null expiry date and empty uploads`() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        whenever(mockState.electricalUploadIds).thenReturn(emptyList())

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyComplianceService).updateElectricalSafety(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            electricalSafetyExpiryDate = null,
            electricalSafetyCertUploadIds = emptyList(),
        )
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())

        whenever(
            mockPropertyComplianceService.updateElectricalSafety(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                electricalSafetyExpiryDate = null,
                electricalSafetyCertUploadIds = emptyList(),
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state and returns the default destination`() {
        val defaultDestination = Destination.ExternalUrl("redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assert(result == defaultDestination)
    }
}
