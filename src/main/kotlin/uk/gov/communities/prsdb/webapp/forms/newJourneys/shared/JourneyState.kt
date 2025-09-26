package uk.gov.communities.prsdb.webapp.forms.newJourneys.shared

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

interface JourneyState {
    val journeyData: JourneyData

    fun addStepData(
        key: String,
        value: Any,
    ): JourneyState
}

interface FooJourneyState :
    OccupiedJourneyState,
    EpcJourneyState

interface OccupiedJourneyState : JourneyState {
    fun numberOfHouseholds(householdsStepKey: String = "households"): Int?
}

interface EpcJourneyState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long
    val searchedForEpcNumber: String?
}

class SimpleJourneyState(
    private val journeyDataService: JourneyDataService,
) : FooJourneyState {
    constructor(journeyDataService: JourneyDataService, propertyInt: Long) : this(journeyDataService) {
        journeyDataService.addToJourneyDataIntoSession(mapOf("propertyId" to propertyInt))
    }

    val innerJourneyData: Map<String, Any?>
        get() = journeyDataService.getJourneyDataFromSession()

    override val journeyData: JourneyData
        get() = objectToStringKeyedMap(innerJourneyData["journeyData"]) ?: emptyMap()

    override fun addStepData(
        key: String,
        value: Any,
    ): JourneyState {
        val newJourneyData = journeyData + (key to value)
        journeyDataService.addToJourneyDataIntoSession(mapOf("journeyData" to newJourneyData))
        return this
    }

    override fun numberOfHouseholds(householdsStepKey: String): Int? =
        objectToStringKeyedMap(journeyData[householdsStepKey])?.get("numberOfHouseholds")?.toString()?.toIntOrNull()

    override val searchedForEpcNumber: String?
        get() = objectToStringKeyedMap(journeyData["search-for-epc"])?.get("certificateNumber")?.toString()

    override var automatchedEpc: EpcDataModel?
        get() =
            innerJourneyData["automatchedEpc"]?.let {
                Json.decodeFromString<EpcDataModel>(it.toString())
            }
        set(value) {
            val encodedEpc = value?.let { Json.encodeToString(it) }
            journeyDataService.addToJourneyDataIntoSession(mapOf("automatchedEpc" to encodedEpc))
        }

    override var searchedEpc: EpcDataModel?
        get() =
            innerJourneyData["searchedEpc"]?.let {
                Json.decodeFromString<EpcDataModel>(it.toString())
            }
        set(value) {
            val encodedEpc = value?.let { Json.encodeToString(it) }
            journeyDataService.addToJourneyDataIntoSession(mapOf("searchedEpc" to encodedEpc))
        }
    override val propertyId: Long
        get() = innerJourneyData["propertyId"]?.toString()?.toLongOrNull()!!
}
