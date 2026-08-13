package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.GovBodyMembersListState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask

interface UpdateCompaniesHouseTaskState :
    OrgCompaniesHouseUpdateState,
    GovBodyMembersListState {
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
    val initialiseGovBodyMembersStep: InitialiseGovBodyMembersStep
    val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep
    val interruptionStep: OrgCompaniesHouseInterruptionStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val govBodyMembersBackRoutingStep: GovBodyMembersBackRoutingStep

    var governingBodyMembersInitialised: Boolean?
}
