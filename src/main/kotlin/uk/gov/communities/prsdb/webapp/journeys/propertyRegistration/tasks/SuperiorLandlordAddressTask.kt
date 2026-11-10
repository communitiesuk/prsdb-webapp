package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask

@JourneyFrameworkComponent
class SuperiorLandlordAddressTask(
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
            "fieldSetHeading" to "registerProperty.superiorLandlords.address.lookup.fieldSetHeading",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf("fieldSetHeading" to "registerProperty.superiorLandlords.address.select.fieldSetHeading")

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.superiorLandlords.address.manual.fieldSetHeading",
            "fieldSetHint" to null,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-address"
    }
}
