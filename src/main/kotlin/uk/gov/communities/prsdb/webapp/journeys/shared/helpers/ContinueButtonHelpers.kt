package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask

fun JourneyBuilder<*>.useContinueButtons(vararg steps: JourneyStep<*, *, *>) {
    steps.forEach { step ->
        configureStep(step) {
            withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
        }
    }
}

fun JourneyBuilder<*>.useContinueButtons(task: GasSafetyDetailsTask) {
    useContinueButtons(task.hasGasSupplyStep, task.hasGasCertStep, task.gasCertIssueDateStep, task.checkGasCertUploadsStep)
    configureStep(task.gasCertExpiredStep) {
        withAdditionalContentProperty {
            "submitButtonText" to if (task.isOccupied) "forms.buttons.continueWithoutGasSafety" else "forms.buttons.continue"
        }
    }
}

fun JourneyBuilder<*>.useContinueButtons(task: GasSafetyTask) {
    useContinueButtons(task.gasSafetyDetailsTask)
    useContinueButtons(task.checkGasSafetyAnswersStep)
}

// TODO PDJB-2039: Add overloads for the electrical safety tasks, replacing UpdateElectricalSafetyJourneyFactory.replaceButtons()
// TODO PDJB-2040: Add overloads for the EPC tasks, replacing UpdateEpcJourneyFactory.replaceButtons()
