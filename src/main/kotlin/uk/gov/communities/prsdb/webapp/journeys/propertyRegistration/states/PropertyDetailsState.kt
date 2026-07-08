package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AddToLandlordIncompletePropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask

interface PropertyDetailsState :
    JourneyState,
    PropertyRegistrationAddressState,
    BedroomsState {
    val addressTask: PropertyRegistrationAddressTask
    val addToLandlordIncompletePropertiesStep: AddToLandlordIncompletePropertiesStep
    val propertyTypeStep: PropertyTypeStep
}
