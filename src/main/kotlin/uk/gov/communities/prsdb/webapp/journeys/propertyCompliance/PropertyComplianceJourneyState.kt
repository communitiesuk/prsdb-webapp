package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceTaskListStep

interface PropertyComplianceJourneyState : JourneyState {
    val taskListStep: PropertyComplianceTaskListStep
    /*val gasSafetyTask: UploadGasSafetyTask
    val eicrTask: UploadEicrTask
    val epcTask: UploadEpcTask*/
}
