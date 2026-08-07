package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCompaniesHouseTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

interface OrgCompaniesHouseChangeState : OrgCompaniesHouseUpdateState {
    val companiesHouseTask: OrgCompaniesHouseTask
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep
    val orgCompaniesHouseInterruptionStep: OrgCompaniesHouseInterruptionStep

    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
        get() = companiesHouseTask.orgIsRegisteredCompanyStep
}

// The only thing the change flow needs from its enclosing journey: the landlord's original Companies House answer, read
// lazily so it reflects the base journey after the reused steps have been built.
fun interface OrgCompaniesHouseChangeDependencies {
    fun previousIsRegisteredCompany(): YesOrNo?
}
