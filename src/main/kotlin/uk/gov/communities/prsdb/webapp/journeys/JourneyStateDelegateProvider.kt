package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import kotlin.reflect.KProperty

@PrsdbWebService
@Scope("prototype")
class JourneyStateDelegateProvider(
    val journeyStateService: JourneyStateService,
) {
    private val propertyKeysInUse = mutableSetOf<String>()

    fun registerPropertyKey(propertyKey: String) {
        if (propertyKeysInUse.contains(propertyKey)) {
            throw JourneyInitialisationException("Property key '$propertyKey' is already in use in this journey state")
        } else {
            propertyKeysInUse.add(propertyKey)
        }
    }

    final inline fun <TJourney, reified TProperty : Any> mutableDelegate(
        propertyKey: String,
    ): MutableJourneyStateDelegate<TJourney, TProperty> {
        registerPropertyKey(propertyKey)
        return MutableJourneyStateDelegate(journeyStateService, propertyKey, serializer())
    }

    final inline fun <TJourney, reified TProperty : Any> requiredDelegate(
        propertyKey: String,
    ): RequiredJourneyStateDelegate<TJourney, TProperty> {
        registerPropertyKey(propertyKey)
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
                AbstractJourneyState.Companion.decodeFromStringOrNull(
                    serializer,
                    it as String,
                )
            }

        operator fun setValue(
            thisRef: TJourney,
            property: KProperty<*>,
            value: TProperty?,
        ) {
            val encodedValue = value?.let { Json.Default.encodeToString(serializer, value) }
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
                    AbstractJourneyState.Companion.decodeFromStringOrNull(
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
}
