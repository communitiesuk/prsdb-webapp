package uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels

import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyStateDeserializer

data class SetJourneyStateRequestModel(
    val journeyId: String,
    val serializedJourneyData: String,
) {
    constructor(journeyDataKey: String, journeyData: FormData) : this(journeyDataKey, objectMapper.writeValueAsString(journeyData))

    fun getJourneyState(): FormData = objectToStringKeyedMap(objectMapper.readValue(serializedJourneyData, Any::class.java)) ?: emptyMap()

    companion object {
        private val objectMapper =
            JsonMapper
                .builder()
                .addModule(SimpleModule().addDeserializer(Map::class.java, JourneyStateDeserializer()))
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build()
    }
}
