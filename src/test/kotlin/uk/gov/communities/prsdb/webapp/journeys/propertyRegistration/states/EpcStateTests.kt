package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
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
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

class EpcStateTests {
    @Test
    fun `acceptedEpcIfReachable returns acceptedEpc when checkUprnMatchedEpcStep is reachable`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc, uprnStepReachable = true, certStepReachable = false)

        assertEquals(epc, state.acceptedEpcIfReachable)
    }

    @Test
    fun `acceptedEpcIfReachable returns acceptedEpc when confirmEpcDetailsRetrievedByCertificateNumberStep is reachable`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc, uprnStepReachable = false, certStepReachable = true)

        assertEquals(epc, state.acceptedEpcIfReachable)
    }

    @Test
    fun `acceptedEpcIfReachable returns acceptedEpc when checkSuperseededEpcStep is reachable`() {
        val epc = mock<EpcDataModel>()
        val state =
            buildTestEpcState(acceptedEpc = epc, uprnStepReachable = false, certStepReachable = false, supersededStepReachable = true)

        assertEquals(epc, state.acceptedEpcIfReachable)
    }

    @Test
    fun `acceptedEpcIfReachable returns null when neither confirm step is reachable`() {
        val epc = mock<EpcDataModel>()
        val state = buildTestEpcState(acceptedEpc = epc, uprnStepReachable = false, certStepReachable = false)

        assertNull(state.acceptedEpcIfReachable)
    }

    @Test
    fun `acceptedEpcIfReachable returns null when acceptedEpc is null and a confirm step is reachable`() {
        val state = buildTestEpcState(acceptedEpc = null, uprnStepReachable = true, certStepReachable = false)

        assertNull(state.acceptedEpcIfReachable)
    }

    private fun buildTestEpcState(
        acceptedEpc: EpcDataModel? = null,
        uprnStepReachable: Boolean = false,
        certStepReachable: Boolean = false,
        supersededStepReachable: Boolean = false,
    ): EpcState =
        object : AbstractJourneyState(journeyStateService = mock()), EpcState {
            override val isOccupied: Boolean? = null
            override val uprn: Long? = null
            override var epcRetrievedByUprn: EpcDataModel? = null
            override var epcRetrievedByCertificateNumber: EpcDataModel? = null
            override var epcRetrievedByCertificateNumberUpdatedSinceUserReview: Boolean? = null
            override var updatedEpcRetrievedByCertificateNumber: EpcDataModel? = null
            override var acceptedEpc: EpcDataModel? = acceptedEpc

            override val checkUprnMatchedEpcStep =
                mock<ConfirmEpcRetrievedByUprnStep>().apply {
                    whenever(this.isStepReachable).thenReturn(uprnStepReachable)
                }

            override val confirmEpcDetailsRetrievedByCertificateNumberStep =
                mock<ConfirmEpcDetailsRetrievedByCertificateNumberStep>().apply {
                    whenever(this.isStepReachable).thenReturn(certStepReachable)
                }

            override val checkSupersededEpcStep =
                mock<EpcSuperseededStep>().apply {
                    whenever(this.isStepReachable).thenReturn(supersededStepReachable)
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
        }
}
