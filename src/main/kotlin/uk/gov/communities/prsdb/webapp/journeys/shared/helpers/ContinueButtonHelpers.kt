package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask

private fun JourneyBuilder<*>.useContinueButtons(vararg steps: JourneyStep<*, *, *>) {
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

fun JourneyBuilder<*>.useContinueButtons(task: ElectricalSafetyDetailsTask) {
    useContinueButtons(
        task.hasElectricalCertStep,
        // TODO PDJB-1679: Remove eicExpiryDateStep when the EIC_ISSUE_DATE flag is removed
        task.eicExpiryDateStep,
        task.eicIssueDateStep,
        task.checkEicUploadsStep,
        task.eicrExpiryDateStep,
        task.checkEicrUploadsStep,
    )
    configureStep(task.eicExpiredStep) {
        withAdditionalContentProperty {
            "submitButtonText" to if (task.isOccupied) "forms.buttons.continueWithoutElectricalSafety" else "forms.buttons.continue"
        }
    }
    configureStep(task.eicrExpiredStep) {
        withAdditionalContentProperty {
            "submitButtonText" to if (task.isOccupied) "forms.buttons.continueWithoutElectricalSafety" else "forms.buttons.continue"
        }
    }
}

fun JourneyBuilder<*>.useContinueButtons(task: ElectricalSafetyTask) {
    useContinueButtons(task.electricalSafetyDetailsTask)
    useContinueButtons(task.checkElectricalSafetyAnswersStep)
}

// TODO PDJB-2040: Add overloads for the EPC tasks, replacing UpdateEpcJourneyFactory.replaceButtons()
