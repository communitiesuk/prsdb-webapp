package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordAddressStep

interface SuperiorLandlordAddressState : JourneyState {
    val superiorLandlordAddressStep: SuperiorLandlordAddressStep
}
