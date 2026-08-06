package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import kotlin.reflect.KProperty

class JourneyStateDelegateProvider(
    val journeyStateService: JourneyStateService,
) : DelegateKeysOwner {
    private val keysInUse = mutableSetOf<String>()

    private var registry: DelegateKeyRegistry? = null

    fun registerKey(propertyKey: String) {
        if (keysInUse.contains(propertyKey)) {
            throw JourneyInitialisationException("Delegate key '$propertyKey' is already in use in this journey")
        } else {
            keysInUse.add(propertyKey)
        }
        registry?.register(scopedKey(propertyKey))
    }

    private var routePrefix: String? = null

    fun bindRoutePrefix(routePrefix: String?) {
        this.routePrefix = routePrefix
    }

    override fun bindKeyRegistry(registry: DelegateKeyRegistry) {
        this.registry = registry
        keysInUse.forEach { registry.register(scopedKey(it)) }
    }

    fun scopedKey(key: String) = routePrefix?.let { "$it/$key" } ?: key

    final inline fun <TJourney, reified TProperty : Any> nullableDelegate(
        propertyKey: String,
    ): NullableJourneyStateDelegate<TJourney, TProperty> {
        registerKey(propertyKey)
        return NullableJourneyStateDelegate(journeyStateService, { scopedKey(propertyKey) }, serializer())
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
        val delegate =
            RequiredJourneyStateDelegate<TJourney, TProperty>(journeyStateService, { scopedKey(propertyKey) }, serializer(), defaultValue)
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
        return RequiredImmutableJourneyStateDelegate(journeyStateService, { scopedKey(propertyKey) }, serializer())
    }

    class NullableJourneyStateDelegate<TJourney, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val keyProvider: () -> String,
        private val serializer: KSerializer<TProperty>,
    ) {
        constructor(
            journeyStateService: JourneyStateService,
            innerKey: String,
            serializer: KSerializer<TProperty>,
        ) : this(journeyStateService, { innerKey }, serializer)

        operator fun getValue(
            thisRef: TJourney,
            property: KProperty<*>,
        ): TProperty? =
            journeyStateService.getValue(keyProvider())?.let {
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
            journeyStateService.setValue(keyProvider(), encodedValue)
        }
    }

    open class RequiredJourneyStateDelegate<TJourney, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val keyProvider: () -> String,
        private val serializer: KSerializer<TProperty>,
        private val startingValue: TProperty?,
    ) {
        operator fun getValue(
            thisRef: TJourney,
            property: KProperty<*>,
        ): TProperty {
            val innerKey = keyProvider()
            val value =
                journeyStateService.getValue(innerKey)?.let {
                    decodeFromStringOrNull(
                        serializer,
                        it as String,
                    )
                } ?: startingValue
            if (value != null) {
                return value
            } else {
                journeyStateService.deleteState()
                throw IllegalStateException("Property $innerKey not found in journey state - deleting state")
            }
        }

        fun getValueOrNull(): TProperty? =
            journeyStateService.getValue(keyProvider())?.let {
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
            journeyStateService.setValue(keyProvider(), encodedValue)
        }
    }

    class RequiredImmutableJourneyStateDelegate<TJourney, TProperty : Any?>(
        private val journeyStateService: JourneyStateService,
        private val keyProvider: () -> String,
        private val serializer: KSerializer<TProperty>,
        startingValue: TProperty? = null,
    ) : RequiredJourneyStateDelegate<TJourney, TProperty>(
            journeyStateService,
            keyProvider,
            serializer,
            startingValue,
        ) {
        override operator fun setValue(
            thisRef: TJourney?,
            property: KProperty<*>?,
            value: TProperty,
        ) {
            val innerKey = keyProvider()
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
