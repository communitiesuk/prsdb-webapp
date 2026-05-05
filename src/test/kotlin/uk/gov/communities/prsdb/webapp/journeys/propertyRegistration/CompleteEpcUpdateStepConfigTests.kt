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
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
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
    fun `afterStepIsReached calls updateEpc with EPC data when EPC is accepted`() {
        val expiryDate = LocalDate(2030, 6, 15)
        val certificateNumber = "1234-5678-9012-3456-7890"
        val epcUrl = "https://example.com/epc/$certificateNumber"
        val acceptedEpc = EpcDataModel(certificateNumber, "1 Example Street", "B", expiryDate)

        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.acceptedEpcIfReachable).thenReturn(acceptedEpc)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)).thenReturn(epcUrl)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyComplianceService).updateEpc(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            epcCertificateUrl = epcUrl,
            epcExpiryDate = expiryDate.toJavaLocalDate(),
            epcEnergyRating = "B",
            tenancyStartedBeforeEpcExpiry = null,
            epcExemptionReason = null,
            epcMeesExemptionReason = null,
        )
    }

    @Test
    fun `afterStepIsReached calls updateEpc with null EPC data when no EPC accepted`() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(null)

        stepConfig.afterStepIsReached(mockState)

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
    fun `afterStepIsReached calls updateEpc with exemption reasons when applicable`() {
        val epcExemptionFormModel = EpcExemptionFormModel().apply { exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION }
        val meesExemptionFormModel = MeesExemptionReasonFormModel().apply { exemptionReason = MeesExemptionReason.HIGH_COST }
        val tenancyFormModel = EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = true }

        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(tenancyFormModel)

        stepConfig.afterStepIsReached(mockState)

        verify(mockPropertyComplianceService).updateEpc(
            propertyOwnershipId = propertyId,
            initialLastModifiedDate = initialLastModifiedDate,
            epcCertificateUrl = null,
            epcExpiryDate = null,
            epcEnergyRating = null,
            tenancyStartedBeforeEpcExpiry = true,
            epcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
            epcMeesExemptionReason = MeesExemptionReason.HIGH_COST,
        )
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.acceptedEpcIfReachable).thenReturn(null)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(null)

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
}
