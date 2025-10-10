package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import kotlin.reflect.KProperty

interface DynamicJourneyState {
    fun getStepData(key: String): PageData?

    fun addStepData(
        key: String,
        value: PageData,
    )
}

interface EpcJourneyState : DynamicJourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val step1: AbstractStep<*, NoInputFormModel, EpcJourneyState>?
    val step2: AbstractStep<*, NoInputFormModel, EpcJourneyState>?
    val step3: AbstractStep<*, NoInputFormModel, EpcJourneyState>?
    val step4: AbstractStep<*, NoInputFormModel, EpcJourneyState>?
}

abstract class AbstractJourney(
    private val journeyStateService: JourneyStateService,
) : DynamicJourneyState {
    abstract fun buildJourneySteps(journeyId: String): Map<String, StepLifecycleOrchestrator>

    protected fun initialise(journeyId: String) {
        journeyStateService.initialise(journeyId)
    }

    override fun getStepData(key: String): PageData? = objectToStringKeyedMap(journeyStateService.getSubmittedStepData()[key])

    override fun addStepData(
        key: String,
        value: PageData,
    ) = journeyStateService.addSingleStepData(key, value)

    fun <TJourney : AbstractJourney, TProperty : Any> delegate(
        propertyKey: String,
        serializer: KSerializer<TProperty>,
    ) = JourneyStateDelegate<TJourney, TProperty>(journeyStateService, propertyKey, serializer)

    fun <TJourney : AbstractJourney, TProperty : Any> compulsoryDelegate(
        propertyKey: String,
        serializer: KSerializer<TProperty>,
    ) = CompulsoryJourneyStateDelegate<TJourney, TProperty>(journeyStateService, propertyKey, serializer)

    class JourneyStateDelegate<TJourney : DynamicJourneyState, TProperty : Any?>(
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
            journeyStateService.setValue(innerKey, mapOf(innerKey to encodedValue))
        }
    }

    class CompulsoryJourneyStateDelegate<TJourney : DynamicJourneyState, TProperty : Any?>(
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
