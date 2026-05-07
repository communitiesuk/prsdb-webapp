package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ElectricalSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.EpcRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.GasSafetyRegistrationCyaSummaryRowsFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@PrsdbWebService
class ComplianceDetailsHelper(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    fun getGasSafetyCyaContent(state: GasSafetyState): Map<String, Any?> {
        val factory = GasSafetyRegistrationCyaSummaryRowsFactory(state)
        return mapOf(
            "gasSupplyRows" to factory.createGasSupplyRows(),
            "gasCertRows" to factory.createCertRows(),
            "gasInsetTextKey" to factory.getInsetTextKey(),
        )
    }

    fun getElectricalSafetyCyaContent(state: ElectricalSafetyState): Map<String, Any?> {
        val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(state)
        return mapOf(
            "electricalRows" to factory.createRows(),
            "electricalInsetTextKey" to factory.getInsetTextKey(),
        )
    }

    fun getEpcCyaContent(state: EpcState): Map<String, Any?> {
        val factory = EpcRegistrationCyaSummaryRowsFactory(epcCertificateUrlProvider, state)
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
