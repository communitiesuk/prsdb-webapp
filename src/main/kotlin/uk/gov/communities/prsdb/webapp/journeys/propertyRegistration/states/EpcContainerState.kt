package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask

interface EpcContainerState :
    JourneyState,
    EpcDetailsTaskDependencies {
    val epcDetailsTask: EpcDetailsTask
    val checkEpcAnswersStep: CheckEpcAnswersStep
}
