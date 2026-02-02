package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.GasSafetyTask

interface PropertyComplianceJourneyState : JourneyState {
    val taskListStep: PropertyComplianceTaskListStep
    val gasSafetyTask: GasSafetyTask
 /*   val eicrTask: UploadEicrTask
    val epcTask: UploadEpcTask*/
}
