package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// Shared by both surfaces that update a landlord's Companies House registration (the standalone update journey and the
// organisation check-your-answers change flow) so the routing step can detect a changed answer in either.
interface OrgCompaniesHouseUpdateState : JourneyState {
    val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep

    // The landlord's Companies House registration answer before this update began (their current DB record).
    val previousIsRegisteredCompany: YesOrNo?
}
