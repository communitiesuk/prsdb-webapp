package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.minus
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
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.CompleteEpcUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.UpdateEpcJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class CompleteEpcUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    private lateinit var mockState: UpdateEpcJourneyState

    @Mock
    private lateinit var mockEpcExemptionStep: EpcExemptionStep

    @Mock
    private lateinit var mockMeesExemptionStep: MeesExemptionStep

    @Mock
    private lateinit var mockEpcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep

    private lateinit var stepConfig: CompleteEpcUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig = CompleteEpcUpdateStepConfig(mockPropertyComplianceService, mockEpcCertificateUrlProvider)
    }

    @Test
    fun `afterStepIsReached calls updateEpc with EPC data, mees exemption and tenancy check when present`() {
        // Arrange
        val expiryDate = DateTimeHelper().getCurrentDateInUK().minus(5, DAY)
        val certificateNumber = "1234-5678-9012-3456-7890"
        val epcUrl = "https://example.com/epc/$certificateNumber"
        val epcEnergyRating = "F"
        val acceptedEpc =
            MockEpcData.createEpcDataModel(
                certificateNumber = certificateNumber,
                energyRating = epcEnergyRating,
                expiryDate = expiryDate,
            )

        setupMockState(
            acceptedEpc = acceptedEpc,
            tenancyStartedBeforeEpcExpiry = false,
            meesExemptionReason = MeesExemptionReason.WALL_INSULATION,
        )
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)).thenReturn(epcUrl)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).updateEpc(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            epcCertificateUrl = epcUrl,
            epcExpiryDate = expiryDate.toJavaLocalDate(),
            epcEnergyRating = epcEnergyRating,
            tenancyStartedBeforeEpcExpiry = false,
            epcExemptionReason = null,
            epcMeesExemptionReason = MeesExemptionReason.WALL_INSULATION,
        )
    }

    @Test
    fun `afterStepIsReached calls updateEpc with an Epc exemption reason`() {
        // Arrange
        setupMockState(epcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).updateEpc(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            epcCertificateUrl = null,
            epcExpiryDate = null,
            epcEnergyRating = null,
            tenancyStartedBeforeEpcExpiry = null,
            epcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
            epcMeesExemptionReason = null,
        )
    }

    @Test
    fun `afterStepIsReached calls updateEpc with null EPC data when no EPC accepted`() {
        // Arrange
        setupMockState()

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).updateEpc(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            epcCertificateUrl = null,
            epcExpiryDate = null,
            epcEnergyRating = null,
            tenancyStartedBeforeEpcExpiry = null,
            epcExemptionReason = null,
            epcMeesExemptionReason = null,
        )
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        setupMockState()

        whenever(
            mockPropertyComplianceService.updateEpc(
                propertyOwnershipId = propertyId,
                initialLastModifiedDate = initialLastModifiedDate,
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

    private fun setupMockState(
        acceptedEpc: EpcDataModel? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        meesExemptionReason: MeesExemptionReason? = null,
    ) {
        val epcExemptionFormModel = epcExemptionReason?.let { EpcExemptionFormModel().apply { exemptionReason = it } }
        val meesExemptionFormModel = meesExemptionReason?.let { MeesExemptionReasonFormModel().apply { exemptionReason = it } }
        val tenancyFormModel =
            tenancyStartedBeforeEpcExpiry?.let {
                EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = it }
            }

        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.acceptedEpcIfStillAccepted).thenReturn(acceptedEpc)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(tenancyFormModel)
    }
}
