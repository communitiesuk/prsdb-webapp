package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

interface OrgCompaniesHouseUpdateState : JourneyState {
    val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
    val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep
}
