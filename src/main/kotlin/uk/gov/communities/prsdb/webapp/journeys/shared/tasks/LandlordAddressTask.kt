package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

// InternationalAddressTask specialised with the field-set content for a landlord's own address (used by the
// registration, change-answers and update flows). Flow-specific extras (e.g. the update flow's submit button/warning)
// are layered on at the DSL call site.
@JourneyFrameworkComponent
class LandlordAddressTask(
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
        mapOf(
            "fieldSetHeading" to "addressForms.addressInUk.landlordRegistration.fieldSetHeading",
        )

    override val addressInUkMissingAnswerErrorKey = "addressForms.addressInUk.landlordRegistration.radios.error.missing"

    override val lookupAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.lookupAddress.landlordRegistration.fieldSetHeading",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.selectAddress.landlordRegistration.fieldSetHeading",
        )

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.manualAddress.landlordRegistration.fieldSetHeading",
            "fieldSetHint" to null,
        )
}
