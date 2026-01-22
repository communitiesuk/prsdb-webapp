package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressService

@JourneyFrameworkComponent
class LookupAddressStepConfig(
    private val addressService: AddressService,
) : AbstractGenericStepConfig<LookupAddressMode, LookupAddressFormModel, AddressState>() {
    override val formModelClass = LookupAddressFormModel::class

    override fun getStepSpecificContent(state: AddressState) =
        mapOf(
            "postcodeLabel" to "forms.lookupAddress.postcode.label",
            "postcodeHint" to "forms.lookupAddress.postcode.hint",
            "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
            "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: AddressState) = "forms/lookupAddressForm"

    override fun mode(state: AddressState) =
        state.cachedAddresses?.let {
            when (it.isEmpty()) {
                true -> LookupAddressMode.NO_ADDRESSES_FOUND
                false -> LookupAddressMode.ADDRESSES_FOUND
            }
        }

    override fun afterStepDataIsAdded(state: AddressState) {
        val formModel = getFormModelFromState(state)
        val houseNameOrNumber = formModel.notNullValue(LookupAddressFormModel::houseNameOrNumber)
        val postcode = formModel.notNullValue(LookupAddressFormModel::postcode)
        state.cachedAddresses = addressService.searchForAddresses(houseNameOrNumber, postcode, restrictToEngland)
    }

    private var restrictToEngland: Boolean = false

    fun restrictToEngland(): LookupAddressStepConfig {
        this.restrictToEngland = true
        return this
    }
}

@JourneyFrameworkComponent
final class LookupAddressStep(
    stepConfig: LookupAddressStepConfig,
) : RequestableStep<LookupAddressMode, LookupAddressFormModel, AddressState>(stepConfig)

enum class LookupAddressMode {
    ADDRESSES_FOUND,
    NO_ADDRESSES_FOUND,
}
