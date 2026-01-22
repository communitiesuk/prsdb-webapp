package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyRegistrationAddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractSelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractSelectAddressStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService

@JourneyFrameworkComponent
class SelectAddressStepConfig(
    private val addressAvailabilityService: AddressAvailabilityService,
) : AbstractSelectAddressStepConfig<SelectAddressMode, PropertyRegistrationAddressState>() {
    override fun mode(state: PropertyRegistrationAddressState) =
        getFormModelFromStateOrNull(state)?.address?.let { selectedAddress ->
            when {
                selectedAddress == MANUAL_ADDRESS_CHOSEN -> SelectAddressMode.MANUAL_ADDRESS
                state.isAddressAlreadyRegistered == true -> SelectAddressMode.ADDRESS_ALREADY_REGISTERED
                else -> SelectAddressMode.ADDRESS_SELECTED
            }
        }

    override fun afterStepDataIsAdded(state: PropertyRegistrationAddressState) {
        val selectedAddress = getFormModelFromState(state).notNullValue(SelectAddressFormModel::address)
        state.isAddressAlreadyRegistered =
            state.getMatchingAddress(selectedAddress)?.uprn?.let { addressAvailabilityService.isAddressOwned(it) }
    }
}

@JourneyFrameworkComponent
final class SelectAddressStep(
    stepConfig: SelectAddressStepConfig,
) : AbstractSelectAddressStep<SelectAddressMode, PropertyRegistrationAddressState>(stepConfig)

enum class SelectAddressMode {
    MANUAL_ADDRESS,
    ADDRESS_ALREADY_REGISTERED,
    ADDRESS_SELECTED,
}
