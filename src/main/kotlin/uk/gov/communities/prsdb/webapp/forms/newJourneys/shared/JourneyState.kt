package uk.gov.communities.prsdb.webapp.forms.newJourneys.shared

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.UsableStep
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

interface FooJourneyState :
    OccupiedJourneyState,
    EpcJourneyState

interface JourneyState {
    val journeyData: JourneyData

    fun addStepData(
        key: String,
        value: Any,
    ): JourneyState
}

interface OccupiedJourneyState : JourneyState {
    val occupied: UsableStep<OccupancyFormModel>?
    val households: UsableStep<NumberOfHouseholdsFormModel>?
    val tenants: UsableStep<NumberOfPeopleFormModel>?
}

interface EpcJourneyState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestion: UsableStep<EpcFormModel>?
    val checkAutomatchedEpc: UsableStep<CheckMatchedEpcFormModel>?
    val searchForEpc: UsableStep<EpcLookupFormModel>?
    val epcNotFound: UsableStep<NoInputFormModel>?
    val epcSuperseded: UsableStep<NoInputFormModel>?
    val checkSearchedEpc: UsableStep<CheckMatchedEpcFormModel>?
}

open class AbstractJourney(
    protected val journeyDataService: JourneyDataService,
) : JourneyState {
    protected val innerJourneyData: Map<String, Any?>
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
}

class InnerEpcJourney(
    journeyDataService: JourneyDataService,
    propertyId: Long,
) : AbstractJourney(journeyDataService) {
    init {
        if (innerJourneyData["propertyId"] == null) {
            journeyDataService.addToJourneyDataIntoSession(mapOf("propertyId" to propertyId))
        }
    }

    var automatchedEpc: EpcDataModel?
        get() =
            innerJourneyData["automatchedEpc"]?.let {
                Json.decodeFromString<EpcDataModel>(it.toString())
            }
        set(value) {
            val encodedEpc = value?.let { Json.encodeToString(it) }
            journeyDataService.addToJourneyDataIntoSession(mapOf("automatchedEpc" to encodedEpc))
        }

    var searchedEpc: EpcDataModel?
        get() =
            innerJourneyData["searchedEpc"]?.let {
                Json.decodeFromString<EpcDataModel>(it.toString())
            }
        set(value) {
            val encodedEpc = value?.let { Json.encodeToString(it) }
            journeyDataService.addToJourneyDataIntoSession(mapOf("searchedEpc" to encodedEpc))
        }

    val propertyId: Long
        get() = innerJourneyData["propertyId"]?.toString()?.toLongOrNull()!!
}
