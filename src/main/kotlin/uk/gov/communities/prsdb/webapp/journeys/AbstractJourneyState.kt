package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap

abstract class AbstractJourneyState(
    private val journeyStateService: JourneyStateService,
) : JourneyState {
    override fun getStepData(key: String): PageData? = objectToStringKeyedMap(journeyStateService.getSubmittedStepData()[key])

    override fun addStepData(
        key: String,
        value: PageData,
    ) = journeyStateService.addSingleStepData(key, value)

    override val journeyId: String
        get() = journeyStateService.journeyId

    override val journeyMetadata: JourneyMetadata
        get() = journeyStateService.journeyMetadata

    override fun deleteJourney() = journeyStateService.deleteState()

    override fun getSubmittedStepData() = journeyStateService.getSubmittedStepData()

    companion object {
        fun <T> decodeFromStringOrNull(
            deserializer: KSerializer<T>,
            json: String,
        ): T? =
            try {
                Json.decodeFromString(deserializer, json)
            } catch (_: IllegalArgumentException) {
                null
            } catch (_: SerializationException) {
                null
            }
    }
}
