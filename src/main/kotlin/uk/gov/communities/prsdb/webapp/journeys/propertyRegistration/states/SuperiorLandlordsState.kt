package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsMembersTask

interface SuperiorLandlordsState :
    JourneyState,
    SuperiorLandlordsListState {
    val anyoneInOwnershipChainStep: AnyoneInOwnershipChainStep
    val superiorLandlordsMembersTask: SuperiorLandlordsMembersTask
    val superiorLandlordsBackRoutingStep: SuperiorLandlordsBackRoutingStep
}
