package uk.gov.communities.prsdb.webapp.theJourneyFramework

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

interface JourneyState {
    val journeyData: JourneyData

    fun addStepData(
        key: String,
        value: Any,
    ): JourneyState
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
