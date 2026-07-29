package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

interface OrgCompaniesHouseState : JourneyState {
    val orgCompaniesHouseStep: OrgCompaniesHouseStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
}
