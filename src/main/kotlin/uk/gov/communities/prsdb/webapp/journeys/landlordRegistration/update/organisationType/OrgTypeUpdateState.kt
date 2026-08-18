package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

interface OrgTypeUpdateState : JourneyState {
    val orgTypeStep: OrgTypeStep
    val orgTypeUpdateRoutingStep: OrgTypeUpdateRoutingStep
}
