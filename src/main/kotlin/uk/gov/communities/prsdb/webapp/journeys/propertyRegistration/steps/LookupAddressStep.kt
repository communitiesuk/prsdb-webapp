package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressService

@JourneyFrameworkComponent
class LookupAddressStepConfig(
    private val addressService: AddressService,
) : AbstractRequestableStepConfig<LookupAddressMode, LookupAddressFormModel, AddressState>() {
    override val formModelClass = LookupAddressFormModel::class

    override fun getStepSpecificContent(state: AddressState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.lookupAddress.propertyRegistration.fieldSetHeading",
            "fieldSetHint" to "forms.lookupAddress.propertyRegistration.fieldSetHint",
            "postcodeLabel" to "forms.lookupAddress.postcode.label",
            "postcodeHint" to "forms.lookupAddress.postcode.hint",
            "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
            "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
            "submitButtonText" to "forms.buttons.continue",
            BACK_URL_ATTR_NAME to LandlordController.LANDLORD_DASHBOARD_URL,
        )

    override fun chooseTemplate(state: AddressState): String = "forms/lookupAddressForm"

    override fun afterStepDataIsAdded(state: AddressState) {
        super.afterStepDataIsAdded(state)
        val lookupInfo = getFormModelFromState(state)
        val houseNameOrNumber = lookupInfo.notNullValue(LookupAddressFormModel::houseNameOrNumber)
        val postcode = lookupInfo.notNullValue(LookupAddressFormModel::postcode)
        state.cachedAddresses =
            addressService.searchForAddresses(
                houseNameOrNumber,
                postcode,
                restrictedToEngland,
            )
    }

    private var restrictedToEngland = false

    fun restrictToEngland(): LookupAddressStepConfig {
        restrictedToEngland = true
        return this
    }

    override fun mode(state: AddressState): LookupAddressMode? =
        state.cachedAddresses?.let {
            when (it.isEmpty()) {
                true -> LookupAddressMode.NO_ADDRESSES_FOUND
                false -> LookupAddressMode.ADDRESSES_FOUND
            }
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
