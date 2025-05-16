package uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyDataDeserializer

data class SetJourneyDataRequestModel(
    val journeyDataKey: String,
    val serializedJourneyData: String,
) {
    constructor(journeyDataKey: String, journeyData: JourneyData) : this(journeyDataKey, objectMapper.writeValueAsString(journeyData))

    fun getJourneyData(): JourneyData = objectToStringKeyedMap(objectMapper.readValue(serializedJourneyData, Any::class.java)) ?: emptyMap()

    companion object {
        private val objectMapper =
            ObjectMapper()
                .registerModule(SimpleModule().addDeserializer(Map::class.java, JourneyDataDeserializer()))
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
