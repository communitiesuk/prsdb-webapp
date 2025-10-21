package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import kotlin.reflect.KProperty

abstract class AbstractJourneyState(
    private val journeyStateService: JourneyStateService,
) : JourneyState {
    override fun getStepData(key: String): PageData? = objectToStringKeyedMap(journeyStateService.getSubmittedStepData()[key])

    override fun addStepData(
        key: String,
        value: PageData,
    ) = journeyStateService.addSingleStepData(key, value)

    fun <TJourney : AbstractJourneyState, TProperty : Any> delegate(
        propertyKey: String,
        serializer: KSerializer<TProperty>,
    ) = JourneyStateDelegate<TJourney, TProperty>(journeyStateService, propertyKey, serializer)

    fun <TJourney : AbstractJourneyState, TProperty : Any> compulsoryDelegate(
        propertyKey: String,
        serializer: KSerializer<TProperty>,
    ) = CompulsoryJourneyStateDelegate<TJourney, TProperty>(journeyStateService, propertyKey, serializer)

    class JourneyStateDelegate<TJourney : JourneyState, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val innerKey: String,
        private val serializer: KSerializer<TProperty>,
    ) {
        operator fun getValue(
            thisRef: TJourney,
            property: KProperty<*>,
        ): TProperty? = journeyStateService.getValue(innerKey)?.let { decodeFromStringOrNull(serializer, it as String) }

        operator fun setValue(
            thisRef: TJourney,
            property: KProperty<*>,
            value: TProperty?,
        ) {
            val encodedValue = value?.let { Json.encodeToString(serializer, value) }
            journeyStateService.setValue(innerKey, encodedValue)
        }
    }

    class CompulsoryJourneyStateDelegate<TJourney : JourneyState, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val innerKey: String,
        private val serializer: KSerializer<TProperty>,
    ) {
        operator fun getValue(
            thisRef: TJourney,
            property: KProperty<*>,
        ): TProperty {
            val value =
                journeyStateService.getValue(innerKey)?.let { decodeFromStringOrNull(serializer, it as String) }
            if (value != null) {
                return value
            } else {
                journeyStateService.deleteState()
                throw IllegalStateException("Property $innerKey not found in journey state - deleting state")
            }
        }
    }

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
