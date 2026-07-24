package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyTask

interface OrgCompaniesHouseState : JourneyState {
    val orgCompaniesHouseStep: OrgCompaniesHouseStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
    val orgGovBodyTask: OrgGovBodyTask
}
