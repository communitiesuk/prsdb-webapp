package uk.gov.communities.prsdb.webapp.journeys

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KProperty

interface DynamicJourneyState {
    val journeyData: JourneyData

    fun addStepData(
        key: String,
        value: PageData,
    ): DynamicJourneyState
}

open class AbstractJourney(
    protected val journeyDataService: JourneyDataService,
) : DynamicJourneyState {
    protected val innerJourneyData: Map<String, Any?>
        get() = journeyDataService.getJourneyDataFromSession()

    override val journeyData: JourneyData
        get() = objectToStringKeyedMap(innerJourneyData["journeyData"]) ?: emptyMap()

    override fun addStepData(
        key: String,
        value: PageData,
    ): DynamicJourneyState {
        val newJourneyData = journeyData + (key to value)
        journeyDataService.addToJourneyDataIntoSession(mapOf("journeyData" to newJourneyData))
        return this
    }

    fun <TJourney : AbstractJourney, TProperty : Any> delegate(
        propertyKey: String,
        serializer: KSerializer<TProperty>,
    ) = JourneyStateDelegate<TJourney, TProperty>(journeyDataService, propertyKey, serializer)

    fun <TJourney : AbstractJourney, TProperty : Any> compulsoryDelegate(
        propertyKey: String,
        serializer: KSerializer<TProperty>,
    ) = CompulsoryJourneyStateDelegate<TJourney, TProperty>(journeyDataService, propertyKey, serializer)
}

class JourneyStateDelegate<TJourney : DynamicJourneyState, TProperty : Any?>(
    private val journeyDataService: JourneyDataService,
    private val innerKey: String,
    private val serializer: KSerializer<TProperty>,
) {
    operator fun getValue(
        thisRef: TJourney,
        property: KProperty<*>,
    ): TProperty? = journeyDataService.getJourneyDataFromSession()[innerKey]?.let { decodeFromStringOrNull(serializer, it as String) }

    operator fun setValue(
        thisRef: TJourney,
        property: KProperty<*>,
        value: TProperty?,
    ) {
        val encodedValue = value?.let { Json.encodeToString(serializer, value) }
        journeyDataService.addToJourneyDataIntoSession(mapOf(innerKey to encodedValue))
    }
}

class CompulsoryJourneyStateDelegate<TJourney : DynamicJourneyState, TProperty : Any?>(
    private val journeyDataService: JourneyDataService,
    private val innerKey: String,
    private val serializer: KSerializer<TProperty>,
) {
    operator fun getValue(
        thisRef: TJourney,
        property: KProperty<*>,
    ): TProperty {
        val value =
            journeyDataService.getJourneyDataFromSession()[innerKey]?.let { decodeFromStringOrNull(serializer, it as String) }
        if (value != null) {
            return value
        } else {
            journeyDataService.deleteJourneyData()
            throw IllegalStateException("Property $innerKey not found in journey state - deleting state")
        }
    }
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
