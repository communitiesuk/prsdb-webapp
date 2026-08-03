package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep

interface OrgCharityState : JourneyState {
    val orgIsRegisteredCharityStep: OrgIsRegisteredCharityStep
    val orgCharityRegisteredWithStep: OrgCharityRegisteredWithStep
    val orgCharityNumberEnglandAndWalesStep: OrgCharityNumberEnglandAndWalesStep
    val orgCharityNumberNorthernIrelandStep: OrgCharityNumberNorthernIrelandStep
    val orgCharityNumberScotlandStep: OrgCharityNumberScotlandStep
}
