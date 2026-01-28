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

    final inline fun <TJourney, reified TProperty : Any> nullableDelegate(
        propertyKey: String,
    ): NullableJourneyStateDelegate<TJourney, TProperty> {
        registerKey(propertyKey)
        return NullableJourneyStateDelegate(journeyStateService, propertyKey, serializer())
    }

    /**
     * Creates a delegate for a required journey state property that must have a value.
     *
     * Usage:
     * ```kotlin
     * var myProperty: PropertyTye by requiredDelegate("myPropertyKey")
     * ```
     *
     * If the property is accessed and no value is found in the journey state, the entire
     * journey state will be deleted and an [IllegalStateException] will be thrown.
     *
     * @param TJourney The journey class type
     * @param TProperty The property type (must be serializable)
     * @param propertyKey Unique key to store this property in the journey state
     * @return A delegate that manages getting and setting the property value
     * @throws JourneyInitialisationException if the propertyKey is already in use
     */
    final inline fun <TJourney, reified TProperty : Any> requiredDelegate(
        propertyKey: String,
        defaultValue: TProperty? = null,
    ): RequiredJourneyStateDelegate<TJourney, TProperty> {
        registerKey(propertyKey)
        val delegate = RequiredJourneyStateDelegate<TJourney, TProperty>(journeyStateService, propertyKey, serializer())

        try {
            if (defaultValue != null && delegate.getValueOrNull() == null) {
                delegate.setValue(null, null, defaultValue)
            }
        } catch (_: NoSuchJourneyException) {
            // Ignore - journey does not exist yet, so we cannot set a default value
        }
        return delegate
    }

    /**
     * Creates a delegate for a required journey state property that must have a value and cannot change.
     *
     * Usage:
     * ```kotlin
     * var myProperty: PropertyTye by requiredDelegate("myPropertyKey")
     * ```
     *
     * If the property is accessed and no value is found in the journey state, the entire
     * journey state will be deleted and an [IllegalStateException] will be thrown.
     *
     * If the property is set multiple times, the first value will be kept and other writes will be ignored.
     *
     * @param TJourney The journey class type
     * @param TProperty The property type (must be serializable)
     * @param propertyKey Unique key to store this property in the journey state
     * @return A delegate that manages getting and setting the property value
     * @throws JourneyInitialisationException if the propertyKey is already in use
     */
    final inline fun <TJourney, reified TProperty : Any> requiredImmutableDelegate(
        propertyKey: String,
    ): RequiredJourneyStateDelegate<TJourney, TProperty> {
        registerKey(propertyKey)
        return RequiredImmutableJourneyStateDelegate(journeyStateService, propertyKey, serializer())
    }

    class NullableJourneyStateDelegate<TJourney, TProperty : Any?>(
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

    open class RequiredJourneyStateDelegate<TJourney, TProperty : Any?>(
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

        fun getValueOrNull(): TProperty? =
            journeyStateService.getValue(innerKey)?.let {
                decodeFromStringOrNull(
                    serializer,
                    it as String,
                )
            }

        open operator fun setValue(
            thisRef: TJourney?,
            property: KProperty<*>?,
            value: TProperty,
        ) {
            val encodedValue = value?.let { Json.encodeToString(serializer, value) }
            journeyStateService.setValue(innerKey, encodedValue)
        }
    }

    class RequiredImmutableJourneyStateDelegate<TJourney, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val innerKey: String,
        private val serializer: KSerializer<TProperty>,
    ) : RequiredJourneyStateDelegate<TJourney, TProperty>(
            journeyStateService,
            innerKey,
            serializer,
        ) {
        override operator fun setValue(
            thisRef: TJourney?,
            property: KProperty<*>?,
            value: TProperty,
        ) {
            val rawValue = journeyStateService.getValue(innerKey)
            if (rawValue == null) {
                val encodedValue = value?.let { Json.encodeToString(serializer, value) }
                journeyStateService.setValue(innerKey, encodedValue)
            } else {
                journeyStateService.deleteState()
                throw IllegalStateException("Property $innerKey is immutable and cannot be updated once it is set - deleting state")
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
