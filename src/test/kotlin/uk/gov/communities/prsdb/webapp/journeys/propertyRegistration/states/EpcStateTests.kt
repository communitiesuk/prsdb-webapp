package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyOccupiedCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.StartEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

class EpcStateTests {
    @Test
    fun `acceptedEpcIfStillAccepted returns acceptedEpc when checkUprnMatchedEpcStep has outcome YES`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc, uprnStepOutcome = YesOrNo.YES)

        assertEquals(epc, state.acceptedEpcIfStillAccepted)
    }

    @Test
    fun `acceptedEpcIfStillAccepted returns acceptedEpc when confirmEpcDetailsRetrievedByCertificateNumberStep has outcome YES`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc, certStepOutcome = YesOrNo.YES)

        assertEquals(epc, state.acceptedEpcIfStillAccepted)
    }

    @Test
    fun `acceptedEpcIfStillAccepted returns acceptedEpc when checkSuperseededEpcStep has outcome COMPLETE`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc, supersededStepOutcome = Complete.COMPLETE)

        assertEquals(epc, state.acceptedEpcIfStillAccepted)
    }

    @Test
    fun `acceptedEpcIfStillAccepted returns null when no confirm step has a positive outcome`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc)

        assertNull(state.acceptedEpcIfStillAccepted)
    }

    @Test
    fun `acceptedEpcIfStillAccepted throws when acceptedEpc is null and a confirm step has positive outcome`() {
        val state = buildTestEpcState(acceptedEpc = null, uprnStepOutcome = YesOrNo.YES)

        assertThrows<PrsdbWebException> { state.acceptedEpcIfStillAccepted }
    }

    private fun buildTestEpcState(
        acceptedEpc: EpcDataModel? = null,
        uprnStepOutcome: YesOrNo? = null,
        certStepOutcome: YesOrNo? = null,
        supersededStepOutcome: Complete? = null,
    ): EpcState =
        object : AbstractJourneyState(journeyStateService = mock()), EpcState {
            override val isOccupied: Boolean? = null
            override val uprn: Long? = null
            override val allowProvideCertificateLaterRoute: Boolean = true
            override var epcRetrievedByUprn: EpcDataModel? = null
            override var epcRetrievedByUprnUpdatedSinceUserReview: Boolean? = null
            override var epcRetrievedByCertificateNumber: EpcDataModel? = null
            override var epcRetrievedByCertificateNumberUpdatedSinceUserReview: Boolean? = null
            override var updatedEpcRetrievedByCertificateNumber: EpcDataModel? = null
            override var acceptedEpc: EpcDataModel? = acceptedEpc

            override val startEpcStep =
                mock<StartEpcStep>().apply {
                    whenever(this.isStepReachable).thenReturn(true)
                }

            override val checkUprnMatchedEpcStep =
                mock<ConfirmEpcRetrievedByUprnStep>().apply {
                    whenever(this.outcome).thenReturn(uprnStepOutcome)
                }

            override val confirmEpcDetailsRetrievedByCertificateNumberStep =
                mock<ConfirmEpcDetailsRetrievedByCertificateNumberStep>().apply {
                    whenever(this.outcome).thenReturn(certStepOutcome)
                }

            override val checkSupersededEpcStep =
                mock<EpcSuperseededStep>().apply {
                    whenever(this.outcome).thenReturn(supersededStepOutcome)
                }

            override val epcLookupByUprnStep = mock<EpcLookupByUprnStep>()
            override val hasEpcStep = mock<HasEpcStep>()
            override val epcAgeCheckStep = mock<EpcAgeCheckStep>()
            override val epcEnergyRatingCheckStep = mock<EpcEnergyRatingCheckStep>()
            override val isPropertyOccupiedCheckStep = mock<PropertyOccupiedCheckStep>()
            override val findYourEpcStep = mock<FindYourEpcStep>()
            override val epcNotFoundStep = mock<EpcNotFoundStep>()
            override val epcInDateAtStartOfTenancyCheckStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
            override val hasMeesExemptionStep = mock<HasMeesExemptionStep>()
            override val meesExemptionStep = mock<MeesExemptionStep>()
            override val lowEnergyRatingStep = mock<LowEnergyRatingStep>()
            override val epcExpiredStep = mock<EpcExpiredStep>()
            override val isEpcRequiredStep = mock<IsEpcRequiredStep>()
            override val epcExemptionStep = mock<EpcExemptionStep>()
            override val epcMissingStep = mock<EpcMissingStep>()
            override val provideEpcLaterStep = mock<ProvideEpcLaterStep>()
            override val checkEpcAnswersStep = mock<CheckEpcAnswersStep>()
            override val epcDetailsTask = mock<EpcDetailsTask>()
        }
}
