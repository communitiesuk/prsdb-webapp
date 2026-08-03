package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.CompleteElectricalSafetyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.UpdateElectricalSafetyJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.UploadService

@ExtendWith(MockitoExtension::class)
class CompleteElectricalSafetyUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockState: UpdateElectricalSafetyJourneyState

    @Mock
    private lateinit var mockElectricalSafetyDetailsTask: ElectricalSafetyDetailsTask

    @Mock
    private lateinit var mockUploadService: UploadService

    private lateinit var stepConfig: CompleteElectricalSafetyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteElectricalSafetyUpdateStepConfig(mockPropertyComplianceService, mockUploadService)
    }

    @Nested
    inner class AfterStepIsReached {
        @BeforeEach
        fun setUp() {
            whenever(mockState.propertyId).thenReturn(propertyId)
            whenever(mockState.electricalSafetyDetailsTask).thenReturn(mockElectricalSafetyDetailsTask)
        }

        @Test
        fun `calls updateElectricalSafety with expiry date and upload ids`() {
            val expiryDate = LocalDate(2026, 6, 15)
            val uploadIds = listOf(1L, 2L)

            whenever(mockState.previousUploadIds).thenReturn(emptyList())
            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockElectricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(CertificateType.Eicr)
            whenever(mockElectricalSafetyDetailsTask.getElectricalCertificateExpiryDateIfReachable()).thenReturn(expiryDate)
            whenever(mockElectricalSafetyDetailsTask.electricalUploadIds).thenReturn(uploadIds)

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyComplianceService).updateElectricalSafety(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                electricalCertType = CertificateType.Eicr,
                electricalSafetyExpiryDate = expiryDate.toJavaLocalDate(),
                electricalSafetyCertUploadIds = uploadIds,
            )
        }

        @Test
        fun `calls updateElectricalSafety with null expiry date and empty uploads`() {
            whenever(mockState.previousUploadIds).thenReturn(emptyList())
            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockElectricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(null)
            whenever(mockElectricalSafetyDetailsTask.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
            whenever(mockElectricalSafetyDetailsTask.electricalUploadIds).thenReturn(emptyList())

            stepConfig.afterStepIsReached(mockState)

            verify(mockPropertyComplianceService).updateElectricalSafety(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                electricalCertType = null,
                electricalSafetyExpiryDate = null,
                electricalSafetyCertUploadIds = emptyList(),
            )
        }

        @Test
        fun `deletes the journey then rethrows when it gets an UpdateConflictException`() {
            // Arrange
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
        fun `deletes each previous file upload`() {
            whenever(mockState.previousUploadIds).thenReturn(mutableListOf(10L, 20L))

            whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
            whenever(mockElectricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(null)
            whenever(mockElectricalSafetyDetailsTask.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
            whenever(mockElectricalSafetyDetailsTask.electricalUploadIds).thenReturn(emptyList())

            stepConfig.afterStepIsReached(mockState)

            verify(mockUploadService).deleteUploadedFile(10L)
            verify(mockUploadService).deleteUploadedFile(20L)
        }
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state and returns the default destination`() {
        val defaultDestination = Destination.ExternalUrl("redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        verify(mockState).deleteJourney()
        assert(result == defaultDestination)
    }
}
