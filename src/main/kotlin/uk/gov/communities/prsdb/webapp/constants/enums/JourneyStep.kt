package uk.gov.communities.prsdb.webapp.constants.enums

import kotlinx.serialization.json.JsonElement

enum class JourneyStep : IJourneyStep {
    PHONE_NUMBER,
}

interface IJourneyStep {
    fun updateContext(
        context: Map<String, JsonElement>,
        formData: Map<String, JsonElement>,
    ): Map<String, JsonElement> {
        // TODO this should be a lamda that defaults do just adding the data and can wbe overwritten to add it in a specific way
        return context + formData
    }

    fun nextStep(context: Map<String, JsonElement>): JourneyStep {
    }
}
