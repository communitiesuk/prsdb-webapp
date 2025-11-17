package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressService

@Scope("prototype")
@PrsdbWebComponent
class LookupAddressStepConfig(
    private val addressService: AddressService,
) : AbstractGenericStepConfig<LookupAddressMode, LookupAddressFormModel, AddressState>() {
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

    override fun afterSubmitFormData(state: AddressState) {
        super.afterSubmitFormData(state)
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

@Scope("prototype")
@PrsdbWebComponent
final class LookupAddressStep(
    stepConfig: LookupAddressStepConfig,
) : RequestableStep<LookupAddressMode, LookupAddressFormModel, AddressState>(stepConfig)

enum class LookupAddressMode {
    ADDRESSES_FOUND,
    NO_ADDRESSES_FOUND,
}
