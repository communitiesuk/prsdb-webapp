package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMustProvideInfoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask

interface OrgGovBodyState :
    JourneyState,
    GovBodyMembersListState,
    GovBodyDetailsModeState {
    val orgGovBodyDetailsStep: OrgGovBodyDetailsStep
    val orgGovBodyMustProvideInfoStep: OrgGovBodyMustProvideInfoStep
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val govBodyMembersBackRoutingStep: GovBodyMembersBackRoutingStep
}
