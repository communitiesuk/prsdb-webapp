package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalAuthorityStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

interface AddressState : JourneyState {
    val lookupStep: LookupAddressStep
    val selectAddressStep: SelectAddressStep
    val alreadyRegisteredStep: AlreadyRegisteredStep
    val noAddressFoundStep: NoAddressFoundStep
    val manualAddressStep: ManualAddressStep
    val localAuthorityStep: LocalAuthorityStep
    var cachedAddresses: List<AddressDataModel>?

    fun getMatchingAddress(address: String): AddressDataModel? = cachedAddresses?.singleOrNull { it.singleLineAddress == address }
}
