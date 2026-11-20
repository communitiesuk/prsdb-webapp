package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.CompleteEpcUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.UpdateEpcJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDetailsDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class CompleteEpcUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockComplianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory

    @Mock
    private lateinit var mockState: UpdateEpcJourneyState

    @Mock
    private lateinit var mockEpcDetailsTask: EpcDetailsTask

    private lateinit var stepConfig: CompleteEpcUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()
    private val epc =
        EpcDetailsDataModel(
            certificateUrl = "https://example.com/epc/1234-5678-9012-3456-7890",
            expiryDate = LocalDate.of(2030, 1, 2),
            energyRating = "F",
            tenancyStartedBeforeExpiry = false,
            exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
            meesExemptionReason = MeesExemptionReason.WALL_INSULATION,
        )

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteEpcUpdateStepConfig(mockPropertyComplianceService, mockComplianceDetailsDataModelFactory)
    }

    @Test
    fun `afterStepIsReached calls updateEpc with the EPC details from the journey`() {
        // Arrange
        setupMockState()

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).updateEpc(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            epc = epc,
        )
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        setupMockState()

        whenever(
            mockPropertyComplianceService.updateEpc(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
                epc = epc,
            ),
        ).thenThrow(UpdateConflictException::class.java)

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

    private fun setupMockState() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.epcDetailsTask).thenReturn(mockEpcDetailsTask)
        whenever(mockComplianceDetailsDataModelFactory.fromEpcDetailState(mockEpcDetailsTask)).thenReturn(epc)
    }
}
