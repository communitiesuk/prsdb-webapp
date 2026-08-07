package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCompaniesHouseTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyTask

interface OrgCompaniesHouseChangeState : JourneyState {
    val companiesHouseTask: OrgCompaniesHouseTask
    val orgGovBodyTask: OrgGovBodyTask
    val orgCompaniesHouseInterruptionStep: OrgCompaniesHouseInterruptionStep
}
