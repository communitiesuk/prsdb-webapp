package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

@Scope("prototype")
@PrsdbWebComponent
class SelectAddressStepConfig(
    private val propertyRegistrationService: PropertyRegistrationService,
) : AbstractGenericStepConfig<SelectAddressMode, SelectAddressFormModel, AddressState>() {
    override val formModelClass = SelectAddressFormModel::class

    override fun getStepSpecificContent(state: AddressState): Map<String, Any?> {
        val lookUpDetails = state.lookupStep.formModel
        val addressRadiosViewModel =
            state.cachedAddresses?.let { addresses ->
                addresses.mapIndexed { index, address ->
                    RadiosButtonViewModel(
                        value = address.singleLineAddress,
                        valueStr = (index + 1).toString(),
                    )
                } +
                    listOf(
                        RadiosDividerViewModel("forms.radios.dividerText"),
                        RadiosButtonViewModel(MANUAL_ADDRESS_CHOSEN, labelMsgKey = "forms.selectAddress.addAddressManually"),
                    )
            }

        return mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
            "submitButtonText" to "forms.buttons.useThisAddress",
            "searchAgainUrl" to
                "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
            "houseNameOrNumber" to lookUpDetails.houseNameOrNumber,
            "postcode" to lookUpDetails.postcode,
            "addressCount" to (state.cachedAddresses?.size ?: 0),
            "options" to addressRadiosViewModel,
        )
    }

    override fun afterValidateSubmittedData(
        bindingResult: BindingResult,
        state: AddressState,
    ) {
        super.afterValidateSubmittedData(bindingResult, state)

        val selectAddressFormModel = bindingResult.target as SelectAddressFormModel
        selectAddressFormModel.address?.let { selectedAddress ->
            when {
                selectedAddress == MANUAL_ADDRESS_CHOSEN -> {}
                state.getMatchingAddress(selectedAddress) == null ->
                    bindingResult.rejectValue(
                        SelectAddressFormModel::address.name,
                        "forms.selectAddress.error.invalidSelection",
                    )
            }
        }
    }

    override fun afterSubmitFormData(state: AddressState) {
        super.afterSubmitFormData(state)
        state.isAddressAlreadyRegistered = state.getAddressOrNull()?.uprn?.let { propertyRegistrationService.getIsAddressRegistered(it) }
    }

    override fun chooseTemplate(state: AddressState): String = "forms/selectAddressForm"

    override fun mode(state: AddressState): SelectAddressMode? =
        getFormModelFromStateOrNull(state)?.address?.let { selectAddress ->
            when {
                selectAddress == MANUAL_ADDRESS_CHOSEN -> SelectAddressMode.MANUAL_ADDRESS
                state.isAddressAlreadyRegistered == true -> SelectAddressMode.ADDRESS_ALREADY_REGISTERED
                else -> SelectAddressMode.ADDRESS_SELECTED
            }
        }
}

@Scope("prototype")
@PrsdbWebComponent
final class SelectAddressStep(
    stepConfig: SelectAddressStepConfig,
) : RequestableStep<SelectAddressMode, SelectAddressFormModel, AddressState>(stepConfig)

enum class SelectAddressMode {
    MANUAL_ADDRESS,
    ADDRESS_ALREADY_REGISTERED,
    ADDRESS_SELECTED,
}
