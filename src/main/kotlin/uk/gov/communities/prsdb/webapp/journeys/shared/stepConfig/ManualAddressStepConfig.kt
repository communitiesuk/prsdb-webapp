package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_ADDRESSES
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

@JourneyFrameworkComponent
class ManualAddressStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<Complete, ManualAddressFormModel, AddressState>() {
    override val formModelClass = ManualAddressFormModel::class

    override fun getStepSpecificContent(state: AddressState) =
        mapOf(
            "fieldSetHint" to "addressForms.manualAddress.fieldSetHint",
            "addressLineOneLabel" to "addressForms.manualAddress.addressLineOne.label",
            "addressLineTwoLabel" to "addressForms.manualAddress.addressLineTwo.label",
            "townOrCityLabel" to "addressForms.manualAddress.townOrCity.label",
            "countyLabel" to "addressForms.manualAddress.county.label",
            "postcodeLabel" to "addressForms.manualAddress.postcode.label",
            "countryLabel" to "addressForms.manualAddress.country.label",
            "isInternationalAddressesEnabled" to featureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES),
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: AddressState) = "forms/manualAddressForm"

    override fun enrichSubmittedDataBeforeValidation(
        state: AddressState,
        formData: FormData,
    ): FormData {
        val isInternationalAddressesEnabled = featureFlagManager.checkFeature(INTERNATIONAL_ADDRESSES)
        val enrichedData =
            super.enrichSubmittedDataBeforeValidation(state, formData) +
                (ManualAddressFormModel::isInternationalAddressesEnabled.name to isInternationalAddressesEnabled)
        return if (isInternationalAddressesEnabled) enrichedData else enrichedData - ManualAddressFormModel::country.name
    }

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
        formModel.country = defaultContent[PREFILL_COUNTRY] as? String
        return defaultContent + (FORM_MODEL_ATTR_NAME to formModel)
    }

    companion object {
        const val PREFILL_ADDRESS_LINE_ONE = "manualPrefillAddressLineOne"
        const val PREFILL_ADDRESS_LINE_TWO = "manualPrefillAddressLineTwo"
        const val PREFILL_TOWN_OR_CITY = "manualPrefillTownOrCity"
        const val PREFILL_COUNTY = "manualPrefillCounty"
        const val PREFILL_POSTCODE = "manualPrefillPostcode"
        const val PREFILL_COUNTRY = "manualPrefillCountry"
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
