package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ElectricalSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.EpcRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.GasSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@PrsdbWebService
class ComplianceDetailsHelper(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    fun <T> getGasSafetyCyaContent(state: T): Map<String, Any?> where T : GasSafetyState, T : CheckYourAnswersJourneyState {
        val factory =
            GasSafetyRegistrationCyaSummaryRowsFactory(state) { step ->
                Destination.VisitableStep(step, state.getCyaJourneyId(step))
            }
        return mapOf(
            "gasSupplyRows" to factory.createGasSupplyRows(),
            "gasCertRows" to factory.createCertRows(),
            "gasInsetTextKey" to factory.getInsetTextKey(),
        )
    }

    fun <T> getElectricalSafetyCyaContent(state: T): Map<String, Any?> where T : ElectricalSafetyState, T : CheckYourAnswersJourneyState {
        val factory =
            ElectricalSafetyRegistrationCyaSummaryRowsFactory(state) { step ->
                Destination.VisitableStep(step, state.getCyaJourneyId(step))
            }
        return mapOf(
            "electricalRows" to factory.createRows(),
            "electricalInsetTextKey" to factory.getInsetTextKey(),
        )
    }

    fun <T> getEpcCyaContent(state: T): Map<String, Any?> where T : EpcState, T : CheckYourAnswersJourneyState {
        val factory =
            EpcRegistrationCyaSummaryRowsFactory(epcCertificateUrlProvider, state) { step ->
                Destination.VisitableStep(step, state.getCyaJourneyId(step))
            }
        return mapOf(
            "epcCardTitle" to factory.createEpcCardTitle(),
            "epcCardActions" to factory.createEpcCardActions(),
            "epcCardRows" to factory.createEpcCardRows(),
            "epcExpiredTextKey" to factory.getEpcExpiredTextKey(),
            "tenancyCheckRows" to factory.createTenancyCheckRows(),
            "lowRatingTextKey" to factory.getLowRatingTextKey(),
            "exemptionReasonRows" to factory.createExemptionReasonRows(),
            "nonEpcRows" to factory.createNonEpcRows(),
            "epcInsetTextKey" to factory.getInsetTextKey(),
        )
    }
}
