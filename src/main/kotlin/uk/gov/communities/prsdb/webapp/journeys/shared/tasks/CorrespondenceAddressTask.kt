package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

// The correspondence (postal) address task. Reuses the shared AddressTask lookup/select/manual flow.
// TODO PDJB-1591: replace the standard lookup page with the rich guidance content page
//  (Figma 41503-119098 — inset text and bullet guidance).
@JourneyFrameworkComponent
class CorrespondenceAddressTask(
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
            "fieldSetHeading" to "addressForms.lookupAddress.correspondence.fieldSetHeading",
            "fieldSetHint" to "addressForms.lookupAddress.correspondence.fieldSetHint",
            "todoContent" to "TODO (PDJB-1591): this lookup page needs the rich guidance content (inset text and bullet guidance)",
        )

    override val selectAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.selectAddress.correspondence.fieldSetHeading",
        )

    override val manualAddressContentProperties: Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "addressForms.manualAddress.correspondence.fieldSetHeading",
            "fieldSetHint" to "addressForms.manualAddress.correspondence.fieldSetHint",
            "todoContent" to "TODO (PDJB-1589): correspondence address skeleton - this page may need content changes",
        )

    companion object {
        const val ROUTE_SEGMENT = "correspondence-address"
    }
}
