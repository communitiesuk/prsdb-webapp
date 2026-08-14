package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.checkAnswersChangeJourneys

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.GovBodyDetailsModeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.GovBodyMembersListState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateState

interface OrgCompaniesHouseChangeGovBodyState :
    OrgCompaniesHouseUpdateState,
    GovBodyMembersListState {
    val interruptionStep: OrgCompaniesHouseInterruptionStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val govBodyMembersBackRoutingStep: GovBodyMembersBackRoutingStep
    val govBodyDetailsModeState: GovBodyDetailsModeState
}

class OrgCompaniesHouseChangeGovBodyDependencies(
    val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    val orgCompanyNumberStep: OrgCompanyNumberStep,
    val orgGovBodyState: OrgGovBodyState,
    val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep,
)
