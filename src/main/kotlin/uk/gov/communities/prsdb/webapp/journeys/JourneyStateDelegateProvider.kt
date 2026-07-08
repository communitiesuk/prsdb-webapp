package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import kotlin.reflect.KProperty

class JourneyStateDelegateProvider(
    val journeyStateService: JourneyStateService,
) {
    private val keysInUse = mutableSetOf<String>()

    // KNOWN LIMITATION (to be addressed in a later commit - do not rely on this guard across providers yet):
    // registerKey was designed to guarantee that, within a single journey state's session storage, no two
    // delegates share a key. That guarantee only holds for delegates registered against the SAME
    // JourneyStateDelegateProvider instance - i.e. the journey state's own provider.
    //
    // Self-stated tasks (e.g. AddressTask) CREATE their own JourneyStateDelegateProvider rather than sharing the
    // journey's, so their (route-scoped) keys register in a separate registry and are never compared against the
    // journey's keys, nor against other tasks' keys. Consequences:
    //   - A task's null-route bare key (e.g. "cachedAddresses") can silently collide with a journey key of the
    //     same name in the shared session storage, undetected.
    //   - Two tasks bound to the SAME route produce identical keys but are not caught.
    //
    // Desired outcome: all delegates (journey and task) register in a single shared location so their keys are
    // compared together and collisions like the above are detected. This is awkward while the provider is created
    // rather than injected, so it is deferred. Note also that, for the access-time keyProvider overload,
    // registration now happens lazily on first get/set (request time) rather than at journey build time.
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

    // Access-time key variant: the key is computed on every get/set rather than fixed at construction. Used by
    // self-stated tasks (e.g. AddressTask) whose route prefix is only known after DI construction, once the
    // TaskInitialiser has called bindRoute. Skips registerKey because the key is not resolvable here; uniqueness
    // is guaranteed by each instance's route prefix and its own provider.
    final inline fun <TJourney, reified TProperty : Any> nullableDelegate(
        noinline keyProvider: () -> String,
    ): NullableJourneyStateDelegate<TJourney, TProperty> = NullableJourneyStateDelegate(journeyStateService, keyProvider, serializer())

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
        val delegate = RequiredJourneyStateDelegate<TJourney, TProperty>(journeyStateService, propertyKey, serializer(), defaultValue)
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
        private val innerKey: String,
        private val serializer: KSerializer<TProperty>,
        private val startingValue: TProperty?,
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
                } ?: startingValue
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
        startingValue: TProperty? = null,
    ) : RequiredJourneyStateDelegate<TJourney, TProperty>(
            journeyStateService,
            innerKey,
            serializer,
            startingValue,
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
