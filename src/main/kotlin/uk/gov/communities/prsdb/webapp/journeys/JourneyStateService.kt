package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory

@Service
@Scope("prototype")
class JourneyStateService(
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    private lateinit var journeyDataService: JourneyDataService

    fun initialise(journeyId: String) {
        journeyDataService = journeyDataServiceFactory.create(journeyId)
    }

    fun getValue(key: String): Any? = journeyDataService.getJourneyDataFromSession()[key]

    fun addSingleStepData(
        key: String,
        value: PageData,
    ) {
        val newJourneyData = getSubmittedStepData() + (key to value)
        setValue(STEP_DATA_KEY, newJourneyData)
    }

    fun getSubmittedStepData() = objectToStringKeyedMap(getValue(STEP_DATA_KEY)) ?: emptyMap()

    fun setValue(
        key: String,
        value: Any?,
    ) {
        journeyDataService.addToJourneyDataIntoSession(mapOf(key to value))
    }

    fun deleteState() {
        journeyDataService.deleteJourneyData()
    }

    companion object {
        private const val STEP_DATA_KEY = "journeyData"
    }
}
