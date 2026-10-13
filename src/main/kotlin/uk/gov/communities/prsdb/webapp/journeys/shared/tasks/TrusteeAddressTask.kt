package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

// AddressTask specialised with the field-set content for an organisation's lead trustee address. Structure and
// route-scoped state come from AddressTask; this only supplies the trustee content.
@JourneyFrameworkComponent
class TrusteeAddressTask(
    journeyStateService: JourneyStateService,
    lookupAddressStep: LookupAddressStep,
    selectAddressStep: SelectAddressStep,
    noAddressFoundStep: NoAddressFoundStep,
    manualAddressStep: ManualAddressStep,
) : AddressTask(
        journeyStateService,
        lookupAddressStep,
        selectAddressStep,
        noAddressFoundStep,
        manualAddressStep,
    ) {
    override val lookupAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.lookupAddress.trusteeRegistration.fieldSetHeading",
            "fieldSetHint" to "addressForms.lookupAddress.trusteeRegistration.fieldSetHint",
        )

    override val selectAddressContentProperties: Map<String, Any?> = emptyMap()

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.manualAddress.trusteeRegistration.fieldSetHeading",
            "fieldSetHint" to null,
        )

    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-address"
    }
}
