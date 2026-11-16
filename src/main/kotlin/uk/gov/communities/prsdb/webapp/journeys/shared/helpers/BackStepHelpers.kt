package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask

fun JourneyBuilder<*>.useCertDateBackStepsFromCheckUploads(task: ElectricalSafetyDetailsTask) {
    configureStep(task.checkEicUploadsStep) {
        backStep {
            // TODO PDJB-1679: Always use the EIC issue date step when the EIC_ISSUE_DATE flag is removed
            if (task.isEicIssueDateEnabled) task.eicIssueDateStep else task.eicExpiryDateStep
        }
    }
    configureStep(task.checkEicrUploadsStep) {
        backStep { task.eicrExpiryDateStep }
    }
}
