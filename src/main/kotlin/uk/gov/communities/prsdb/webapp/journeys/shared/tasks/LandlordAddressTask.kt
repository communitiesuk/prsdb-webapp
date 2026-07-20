package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

// AddressTask specialised with the field-set content for a landlord's own address (used by the registration,
// change-answers and update flows). Structure and route-scoped state come from AddressTask; this only supplies
// the landlord content. Genuinely flow-specific extras (e.g. the update flow's submit button/warning) are still
// layered on at the DSL call site.
@JourneyFrameworkComponent
class LandlordAddressTask(
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
            "fieldSetHeading" to "forms.lookupAddress.landlordRegistration.fieldSetHeading",
            "fieldSetHint" to "forms.lookupAddress.landlordRegistration.fieldSetHint",
        )

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.manualAddress.landlordRegistration.fieldSetHeading",
            "fieldSetHint" to "forms.manualAddress.landlordRegistration.fieldSetHint",
        )
}
