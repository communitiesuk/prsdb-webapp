package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationForOrgLandlordTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationOrgRedesignTask

interface LandlordRegistrationOrgRedesignState :
    LandlordRegistrationState,
    LandlordRegistrationOrgLandlordState {
    val landlordRegistrationOrgRedesignTask: LandlordRegistrationOrgRedesignTask
    val landlordTypeStep: LandlordTypeStep
    val landlordRegistrationForOrgLandlordTask: LandlordRegistrationForOrgLandlordTask
}
