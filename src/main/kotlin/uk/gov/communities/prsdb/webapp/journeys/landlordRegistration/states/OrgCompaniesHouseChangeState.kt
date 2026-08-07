package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCompaniesHouseTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

interface OrgCompaniesHouseChangeState : JourneyState {
    val companiesHouseTask: OrgCompaniesHouseTask
    val orgGovBodyTask: OrgGovBodyTask
    val orgCompaniesHouseInterruptionStep: OrgCompaniesHouseInterruptionStep

    val originalIsRegisteredCompany: YesOrNo?
}

// The only thing the change flow needs from its enclosing journey: the landlord's original Companies House answer. It is
// read lazily (as a function) so it is evaluated after the reused steps have been built, and kept minimal so the task
// does not depend on the whole enclosing check-your-answers state.
fun interface OrgCompaniesHouseChangeDependencies {
    fun originalIsRegisteredCompany(): YesOrNo?
}
