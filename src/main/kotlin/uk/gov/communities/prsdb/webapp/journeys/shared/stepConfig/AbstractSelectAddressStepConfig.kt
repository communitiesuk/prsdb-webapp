package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel

abstract class AbstractSelectAddressStepConfig<TEnum : Enum<TEnum>, TState : AddressState> :
    AbstractGenericStepConfig<TEnum, SelectAddressFormModel, TState>() {
    override val formModelClass = SelectAddressFormModel::class

    override fun getStepSpecificContent(state: TState): Map<String, Any?> {
        val lookedUpAddresses =
            state.cachedAddresses
                ?: throw NotNullFormModelValueIsNullException("No cached addresses found in AddressState")

        val addressRadiosViewModel =
            lookedUpAddresses.mapIndexed { index, address ->
                RadiosButtonViewModel(address.singleLineAddress, valueStr = (index + 1).toString())
            } +
                listOf(
                    RadiosDividerViewModel("forms.radios.dividerText"),
                    RadiosButtonViewModel(MANUAL_ADDRESS_CHOSEN, labelMsgKey = "forms.selectAddress.addAddressManually"),
                )

        return mapOf(
            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
            "submitButtonText" to "forms.buttons.useThisAddress",
            "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
            "houseNameOrNumber" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::houseNameOrNumber),
            "postcode" to state.lookupAddressStep.formModel.notNullValue(LookupAddressFormModel::postcode),
            "addressCount" to lookedUpAddresses.size,
            "options" to addressRadiosViewModel,
        )
    }

    override fun chooseTemplate(state: TState) = "forms/selectAddressForm"

    override fun afterPrimaryValidation(
        state: TState,
        bindingResult: BindingResult,
    ) {
        val selectAddressFormModel = bindingResult.target as SelectAddressFormModel
        selectAddressFormModel.address?.let { selectedAddress ->
            if (selectedAddress != MANUAL_ADDRESS_CHOSEN && state.getMatchingAddress(selectedAddress) == null) {
                bindingResult.rejectValue(SelectAddressFormModel::address.name, "forms.selectAddress.error.invalidSelection")
            }
        }
    }
}

abstract class AbstractSelectAddressStep<TEnum : Enum<TEnum>, TState : AddressState>(
    stepConfig: AbstractSelectAddressStepConfig<TEnum, TState>,
) : RequestableStep<TEnum, SelectAddressFormModel, TState>(stepConfig)
