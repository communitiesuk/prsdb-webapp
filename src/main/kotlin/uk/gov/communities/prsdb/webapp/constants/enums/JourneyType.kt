package uk.gov.communities.prsdb.webapp.constants.enums

import kotlinx.serialization.json.JsonElement
import uk.gov.communities.prsdb.webapp.models.journeyModels.JourneyStep
import uk.gov.communities.prsdb.webapp.models.journeyModels.Step

enum class JourneyType : IJourneyType {
    LANDLORD_REGISTRATION,
}

interface IJourneyType(
    steps: List<Step>,
) {
    fun resolveNext(
        context: Map<String, JsonElement>,
        currentStep: uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep,
    ): JourneyStep {
        val nextStep = currentStep.nextStep(context)
//        return currentStep
    }

    fun validateFormContextForStep(
        journeyStep: uk.gov.communities.prsdb.webapp.constants.enums.JourneyStep,
        context: Map<String, JsonElement>?,
    ): Boolean {
        // TODO get step
        // validate context rules for step
        return true
    }
}
