package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode.ADDRESS_ALREADY_REGISTERED
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode.ADDRESS_SELECTED
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode.MANUAL_ADDRESS
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService

@JourneyFrameworkComponent
class SelectAddressStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractGenericStepConfig<SelectAddressMode, SelectAddressFormModel, AddressState>() {
    override val formModelClass = SelectAddressFormModel::class

    override fun getStepSpecificContent(state: AddressState): Map<String, Any?> {
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

    override fun chooseTemplate(state: AddressState) = "forms/selectAddressForm"

    override fun mode(state: AddressState) =
        getFormModelFromStateOrNull(state)?.address?.let { selectedAddress ->
            when {
                selectedAddress == MANUAL_ADDRESS_CHOSEN -> MANUAL_ADDRESS
                state.isAddressAlreadyRegistered == true -> ADDRESS_ALREADY_REGISTERED
                else -> ADDRESS_SELECTED
            }
        }

    override fun afterPrimaryValidation(
        state: AddressState,
        bindingResult: BindingResult,
    ) {
        val selectAddressFormModel = bindingResult.target as SelectAddressFormModel
        selectAddressFormModel.address?.let { selectedAddress ->
            if (selectedAddress != MANUAL_ADDRESS_CHOSEN && state.getMatchingAddress(selectedAddress) == null) {
                bindingResult.rejectValue(SelectAddressFormModel::address.name, "forms.selectAddress.error.invalidSelection")
            }
        }
    }

    override fun afterStepDataIsAdded(state: AddressState) {
        val selectedAddress = getFormModelFromState(state).notNullValue(SelectAddressFormModel::address)
        state.isAddressAlreadyRegistered =
            state.getMatchingAddress(selectedAddress)?.uprn?.let { addressAvailabilityService.isAddressOwned(it) }
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
