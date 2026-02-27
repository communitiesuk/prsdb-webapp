package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

interface AddressSearchState : JourneyState {
    val lookupAddressStep: LookupAddressStep
    var cachedAddresses: List<AddressDataModel>?
}
