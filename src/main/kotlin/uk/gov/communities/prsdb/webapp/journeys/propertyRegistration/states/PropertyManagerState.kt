package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckPropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemovePropertyManagerStep

interface PropertyManagerState : JourneyState {
    val propertyManagerStep: PropertyManagerStep
    val propertyManagerDetailsStep: PropertyManagerDetailsStep
    val checkPropertyManagerStep: CheckPropertyManagerStep
    val removePropertyManagerStep: RemovePropertyManagerStep
}
