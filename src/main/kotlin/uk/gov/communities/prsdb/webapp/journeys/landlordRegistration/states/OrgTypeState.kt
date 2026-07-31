package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LeadTrusteeTask

interface OrgTypeState : JourneyState {
    val orgTypeStep: OrgTypeStep
    val leadTrusteeTask: LeadTrusteeTask
}
