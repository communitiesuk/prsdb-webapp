package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import kotlin.reflect.KProperty

@JourneyFrameworkComponent
class JourneyStateDelegateProvider(
    val journeyStateService: JourneyStateService,
) {
    private val keysInUse = mutableSetOf<String>()

    fun registerKey(propertyKey: String) {
        if (keysInUse.contains(propertyKey)) {
            throw JourneyInitialisationException("Property key '$propertyKey' is already in use in this journey state")
        } else {
            keysInUse.add(propertyKey)
        }
    }

    final inline fun <TJourney, reified TProperty : Any> mutableDelegate(
        propertyKey: String,
    ): MutableJourneyStateDelegate<TJourney, TProperty> {
        registerKey(propertyKey)
        return MutableJourneyStateDelegate(journeyStateService, propertyKey, serializer())
    }

    final inline fun <TJourney, reified TProperty : Any> requiredDelegate(
        propertyKey: String,
    ): RequiredJourneyStateDelegate<TJourney, TProperty> {
        registerKey(propertyKey)
        return RequiredJourneyStateDelegate(journeyStateService, propertyKey, serializer())
    }

    class MutableJourneyStateDelegate<TJourney, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val innerKey: String,
        private val serializer: KSerializer<TProperty>,
    ) {
        operator fun getValue(
            thisRef: TJourney,
            property: KProperty<*>,
        ): TProperty? =
            journeyStateService.getValue(innerKey)?.let {
                decodeFromStringOrNull(
                    serializer,
                    it as String,
                )
            }

        operator fun setValue(
            thisRef: TJourney,
            property: KProperty<*>,
            value: TProperty?,
        ) {
            val encodedValue = value?.let { Json.encodeToString(serializer, value) }
            journeyStateService.setValue(innerKey, encodedValue)
        }
    }

    class RequiredJourneyStateDelegate<TJourney, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val innerKey: String,
        private val serializer: KSerializer<TProperty>,
    ) {
        operator fun getValue(
            thisRef: TJourney,
            property: KProperty<*>,
        ): TProperty {
            val value =
                journeyStateService.getValue(innerKey)?.let {
                    decodeFromStringOrNull(
                        serializer,
                        it as String,
                    )
                }
            if (value != null) {
                return value
            } else {
                journeyStateService.deleteState()
                throw IllegalStateException("Property $innerKey not found in journey state - deleting state")
            }
        }
    }

    companion object {
        private fun <T> decodeFromStringOrNull(
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
