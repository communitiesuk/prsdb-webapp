package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService

@JourneyFrameworkComponent
class SelectAddressStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractGenericRequestableStepConfig<SelectAddressMode, SelectAddressFormModel, AddressState>() {
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
            "searchAgainUrl" to Destination(state.lookupStep).toUrlStringOrNull(),
            "houseNameOrNumber" to lookUpDetails.houseNameOrNumber,
            "postcode" to lookUpDetails.postcode,
            "addressCount" to (state.cachedAddresses?.size ?: 0),
            "options" to addressRadiosViewModel,
        )
    }

    override fun afterPrimaryValidation(
        state: AddressState,
        bindingResult: BindingResult,
    ) {
        super.afterPrimaryValidation(state, bindingResult)

        val selectAddressFormModel = bindingResult.target as SelectAddressFormModel
        selectAddressFormModel.address?.let { selectedAddress ->
            when {
                selectedAddress == MANUAL_ADDRESS_CHOSEN -> {}

                state.getMatchingAddress(selectedAddress) == null -> {
                    bindingResult.rejectValue(
                        SelectAddressFormModel::address.name,
                        "forms.selectAddress.error.invalidSelection",
                    )
                }
            }
        }
    }

    override fun afterStepDataIsAdded(state: AddressState) {
        super.afterStepDataIsAdded(state)
        state.isAddressAlreadyRegistered =
            state.getAddressOrNull()?.uprn?.let { addressAvailabilityService.isAddressOwned(it) }
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

@JourneyFrameworkComponent
final class SelectAddressStep(
    stepConfig: SelectAddressStepConfig,
) : RequestableStep<SelectAddressMode, SelectAddressFormModel, AddressState>(stepConfig)

enum class SelectAddressMode {
    MANUAL_ADDRESS,
    ADDRESS_ALREADY_REGISTERED,
    ADDRESS_SELECTED,
}
