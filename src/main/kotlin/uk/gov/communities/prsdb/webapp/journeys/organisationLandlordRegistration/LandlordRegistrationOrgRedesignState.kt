package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.tasks.LandlordRegistrationOrgLandlordTask
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.tasks.LandlordRegistrationOrgRedesignTask

interface LandlordRegistrationOrgRedesignState :
    LandlordRegistrationState,
    LandlordRegistrationOrgLandlordState {
    val landlordRegistrationOrgRedesignTask: LandlordRegistrationOrgRedesignTask
    val landlordTypeStep: LandlordTypeStep
    override val landlordRegistrationOrgLandlordTask: LandlordRegistrationOrgLandlordTask
}
