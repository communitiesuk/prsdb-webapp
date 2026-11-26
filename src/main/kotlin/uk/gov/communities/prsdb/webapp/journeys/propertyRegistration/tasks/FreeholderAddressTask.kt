package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.InternationalAddressTask

@JourneyFrameworkComponent
class FreeholderAddressTask(
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
            "fieldSetHeading" to "registerProperty.freeholders.address.inUk.fieldSetHeading",
        )

    override val addressInUkMissingAnswerErrorKey = "registerProperty.freeholders.address.inUk.error.missing"

    override val lookupAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.address.lookup.fieldSetHeading",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf("fieldSetHeading" to "registerProperty.freeholders.address.select.fieldSetHeading")

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholders.address.manual.fieldSetHeading",
            "fieldSetHint" to null,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override val internationalManualAddressContentProperties: Map<String, Any?> =
        manualAddressContentProperties +
            ("fieldSetHeading" to "registerProperty.freeholders.address.nonUk.fieldSetHeading")

    companion object {
        const val ROUTE_SEGMENT = "freeholder-address"
    }
}
