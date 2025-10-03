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

interface JourneyState {
    val journeyData: JourneyData

    fun addStepData(
        key: String,
        value: Any,
    ): JourneyState
}

interface OccupiedJourneyState : JourneyState {
    val occupiedStep: UsableStep<OccupancyFormModel>
    val householdsStep: UsableStep<NumberOfHouseholdsFormModel>
    val tenantsStep: UsableStep<NumberOfPeopleFormModel>
}

interface EpcJourneyState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestionStep: UsableStep<EpcFormModel>
    val checkAutomatchedEpcStep: UsableStep<CheckMatchedEpcFormModel>
    val searchForEpcStep: UsableStep<EpcLookupFormModel>
    val epcNotFoundStep: UsableStep<NoInputFormModel>
    val epcSupersededStep: UsableStep<NoInputFormModel>
    val checkSearchedEpcStep: UsableStep<CheckMatchedEpcFormModel>
}

class FooJourneyState(
    private val journeyDataService: JourneyDataService,
) : OccupiedJourneyState,
    EpcJourneyState {
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

    override lateinit var epcQuestionStep: UsableStep<EpcFormModel>
        private set
    override lateinit var checkAutomatchedEpcStep: UsableStep<CheckMatchedEpcFormModel>
        private set
    override lateinit var searchForEpcStep: UsableStep<EpcLookupFormModel>
        private set
    override lateinit var epcNotFoundStep: UsableStep<NoInputFormModel>
        private set
    override lateinit var epcSupersededStep: UsableStep<NoInputFormModel>
        private set
    override lateinit var checkSearchedEpcStep: UsableStep<CheckMatchedEpcFormModel>
        private set
    override lateinit var occupiedStep: UsableStep<OccupancyFormModel>
        private set
    override lateinit var householdsStep: UsableStep<NumberOfHouseholdsFormModel>
        private set
    override lateinit var tenantsStep: UsableStep<NumberOfPeopleFormModel>
        private set

    companion object {
        fun withSteps(
            journeyDataService: JourneyDataService,
            propertyId: Long,
            epcQuestionStep: UsableStep<EpcFormModel>,
            checkAutomatchedEpcStep: UsableStep<CheckMatchedEpcFormModel>,
            searchForEpcStep: UsableStep<EpcLookupFormModel>,
            epcNotFoundStep: UsableStep<NoInputFormModel>,
            epcSupersededStep: UsableStep<NoInputFormModel>,
            checkSearchedEpcStep: UsableStep<CheckMatchedEpcFormModel>,
            occupiedStep: UsableStep<OccupancyFormModel>,
            householdsStep: UsableStep<NumberOfHouseholdsFormModel>,
            tenantsStep: UsableStep<NumberOfPeopleFormModel>,
        ): FooJourneyState =
            FooJourneyState(journeyDataService, propertyId).apply {
                this.epcQuestionStep = epcQuestionStep
                this.checkAutomatchedEpcStep = checkAutomatchedEpcStep
                this.searchForEpcStep = searchForEpcStep
                this.epcNotFoundStep = epcNotFoundStep
                this.epcSupersededStep = epcSupersededStep
                this.checkSearchedEpcStep = checkSearchedEpcStep
                this.occupiedStep = occupiedStep
                this.householdsStep = householdsStep
                this.tenantsStep = tenantsStep
            }
    }
}
