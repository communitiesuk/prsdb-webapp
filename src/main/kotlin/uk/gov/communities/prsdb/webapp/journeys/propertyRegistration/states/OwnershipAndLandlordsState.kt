package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsPropertyRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTaskDependencies

interface OwnershipAndLandlordsState :
    JourneyState,
    InviteJointLandlordsTaskDependencies {
    val ownershipTypeStep: OwnershipTypeStep
    val jointLandlordsTask: JointLandlordsPropertyRegistrationTask
}
