package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask

interface GasSafetyContainerState :
    JourneyState,
    GasSafetyDetailsTaskDependencies {
    val gasSafetyDetailsTask: GasSafetyDetailsTask
    val checkGasSafetyAnswersStep: CheckGasSafetyAnswersStep
}
