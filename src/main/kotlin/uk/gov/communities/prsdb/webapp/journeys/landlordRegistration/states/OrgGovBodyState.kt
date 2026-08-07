package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMustProvideInfoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask

// The registration governing body flow: the "provide details about your governing body" gate (and its must-provide-info
// dead-end) followed by the shared member-management flow. The member-management flow lives in the nested members task;
// consumers reach member data and steps via orgGovBodyMembersTask. This wrapper does NOT implement OrgGovBodyMembersState
// itself - doing so via delegation would forward the wrapper's whole JourneyState (and its delegate-key binding) to the
// members task instance, double-registering the members task's delegate keys when it is also mounted as a sub-task.
interface OrgGovBodyState : JourneyState {
    val orgGovBodyDetailsStep: OrgGovBodyDetailsStep
    val orgGovBodyMustProvideInfoStep: OrgGovBodyMustProvideInfoStep
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
}
