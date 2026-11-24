package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

@JourneyFrameworkComponent
class OrgInternationalAddressTask(
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
            "fieldSetHeading" to "addressForms.addressInUk.organisationLandlordRegistration.fieldSetHeading",
        )

    override val addressInUkMissingAnswerErrorKey = "addressForms.addressInUk.organisationLandlordRegistration.radios.error.missing"

    override val lookupAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.lookupAddress.organisationLandlordRegistration.fieldSetHeading",
            "fieldSetHint" to "addressForms.lookupAddress.organisationLandlordRegistration.fieldSetHint",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.selectAddress.organisationLandlordRegistration.fieldSetHeading",
        )

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.manualAddress.organisationLandlordRegistration.fieldSetHeading",
            "fieldSetHint" to "addressForms.manualAddress.organisationLandlordRegistration.fieldSetHint",
        )

    companion object {
        const val ROUTE_SEGMENT = "organisation-address"
    }
}
