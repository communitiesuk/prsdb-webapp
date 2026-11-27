package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

// InternationalAddressTask specialised with the content for an organisation's lead trustee address.
@JourneyFrameworkComponent
class TrusteeAddressTask(
    journeyStateService: JourneyStateService,
    addressInUkStep: AddressInUkStep,
    lookupAddressStep: LookupAddressStep,
    selectAddressStep: SelectAddressStep,
    noAddressFoundStep: NoAddressFoundStep,
    manualAddressStep: ManualAddressStep,
    internationalManualAddressStep: ManualAddressStep,
) : InternationalAddressTask(
        journeyStateService,
        addressInUkStep,
        lookupAddressStep,
        selectAddressStep,
        noAddressFoundStep,
        manualAddressStep,
        internationalManualAddressStep,
    ) {
    override val addressInUkContentProperties: Map<String, Any?> =
        mapOf("fieldSetHeading" to "addressForms.addressInUk.trusteeRegistration.fieldSetHeading")

    override val addressInUkMissingAnswerErrorKey = "addressForms.addressInUk.trusteeRegistration.radios.error.missing"

    override val lookupAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.lookupAddress.trusteeRegistration.fieldSetHeading",
            "fieldSetHint" to "addressForms.lookupAddress.trusteeRegistration.fieldSetHint",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf("title" to "addressForms.selectAddress.trusteeRegistration.pageTitle")

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.manualAddress.trusteeRegistration.fieldSetHeading",
            "fieldSetHint" to null,
        )

    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-address"
    }
}
