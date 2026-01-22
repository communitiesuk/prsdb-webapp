package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractSelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

interface AddressState : JourneyState {
    val lookupAddressStep: LookupAddressStep
    val noAddressFoundStep: NoAddressFoundStep
    val selectAddressStep: AbstractSelectAddressStep<*, *>
    val manualAddressStep: ManualAddressStep
    var cachedAddresses: List<AddressDataModel>?

    fun getManualAddressOrNull(): AddressDataModel?

    fun getMatchingAddress(address: String): AddressDataModel? = cachedAddresses?.singleOrNull { it.singleLineAddress == address }

    fun getAddress(): AddressDataModel {
        val selectedAddress = selectAddressStep.formModelOrNull?.address?.let { getMatchingAddress(it) }
        return selectedAddress ?: getManualAddressOrNull() ?: throw NotNullFormModelValueIsNullException("No address found in AddressState")
    }
}
