package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

interface OrgCompaniesHouseState : JourneyState {
    val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
}
