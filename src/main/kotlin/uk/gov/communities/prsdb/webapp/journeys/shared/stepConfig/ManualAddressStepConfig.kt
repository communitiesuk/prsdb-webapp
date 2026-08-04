package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

@JourneyFrameworkComponent
class ManualAddressStepConfig : AbstractRequestableStepConfig<Complete, ManualAddressFormModel, AddressState>() {
    override val formModelClass = ManualAddressFormModel::class

    override fun getStepSpecificContent(state: AddressState) =
        mapOf(
            "fieldSetHint" to "forms.manualAddress.fieldSetHint",
            "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
            "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
            "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
            "countyLabel" to "forms.manualAddress.county.label",
            "postcodeLabel" to "forms.manualAddress.postcode.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: AddressState) = "forms/manualAddressForm"

    override fun resolvePageContent(
        state: AddressState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val prefillAddressLineOne = defaultContent[PREFILL_ADDRESS_LINE_ONE] as? String ?: return defaultContent
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? ManualAddressFormModel ?: return defaultContent
        if (!formModel.addressLineOne.isNullOrBlank()) return defaultContent
        formModel.addressLineOne = prefillAddressLineOne
        formModel.addressLineTwo = defaultContent[PREFILL_ADDRESS_LINE_TWO] as? String
        formModel.townOrCity = defaultContent[PREFILL_TOWN_OR_CITY] as? String
        formModel.county = defaultContent[PREFILL_COUNTY] as? String
        formModel.postcode = defaultContent[PREFILL_POSTCODE] as? String
        return defaultContent + (FORM_MODEL_ATTR_NAME to formModel)
    }

    companion object {
        const val PREFILL_ADDRESS_LINE_ONE = "manualPrefillAddressLineOne"
        const val PREFILL_ADDRESS_LINE_TWO = "manualPrefillAddressLineTwo"
        const val PREFILL_TOWN_OR_CITY = "manualPrefillTownOrCity"
        const val PREFILL_COUNTY = "manualPrefillCounty"
        const val PREFILL_POSTCODE = "manualPrefillPostcode"
    }

    override fun mode(state: AddressState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ManualAddressStep(
    stepConfig: ManualAddressStepConfig,
) : RequestableStep<Complete, ManualAddressFormModel, AddressState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "manual-address"
    }
}
