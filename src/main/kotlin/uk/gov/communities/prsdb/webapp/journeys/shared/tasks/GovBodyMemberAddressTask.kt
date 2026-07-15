package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

@JourneyFrameworkComponent
class GovBodyMemberAddressTask(
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
            "fieldSetHeading" to "forms.lookupAddress.govBodyMemberRegistration.fieldSetHeading",
            "fieldSetHint" to "forms.lookupAddress.govBodyMemberRegistration.fieldSetHint",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.selectAddress.govBodyMemberRegistration.fieldSetHeading",
        )

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "forms.manualAddress.govBodyMemberRegistration.fieldSetHeading",
            "fieldSetHint" to null,
        )

    companion object {
        const val GOV_BODY_MEMBER_ADDRESS_ROUTE_SEGMENT = "governing-body-member-address"
    }
}
