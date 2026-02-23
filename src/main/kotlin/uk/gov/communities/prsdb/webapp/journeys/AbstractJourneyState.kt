package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap

abstract class AbstractJourneyState(
    private val journeyStateService: JourneyStateService,
) : JourneyState {
    override fun getStepData(key: String): PageData? = objectToStringKeyedMap(journeyStateService.getSubmittedStepData()[key])

    override fun addStepData(
        key: String,
        value: PageData,
    ) = journeyStateService.addSingleStepData(key, value)

    override fun clearStepData(key: String) = journeyStateService.clearStepData(key)

    override val journeyId: String
        get() = journeyStateService.journeyId

    override val journeyMetadata: JourneyMetadata
        get() = journeyStateService.journeyMetadata

    override fun deleteJourney() = journeyStateService.deleteState()

    override fun getSubmittedStepData() = journeyStateService.getSubmittedStepData()

    override fun initializeState(seed: Any?): String {
        val journeyId = generateJourneyId(seed)

        journeyStateService.initialiseJourneyWithId(journeyId) {}
        return journeyId
    }

    override fun initializeOrRestoreState(seed: Any?): String {
        val journeyId = generateJourneyId(seed)

        journeyStateService.initialiseOrRestoreJourneyWithId(journeyId) {}
        return journeyId
    }

    override fun save(): SavedJourneyState = journeyStateService.save()

    override fun initializeChildState(
        childJourneyName: String,
        seed: Any?,
    ): String {
        val newJourneyId = generateJourneyId(seed)

        journeyStateService.initialiseChildJourney(newJourneyId, childJourneyName)
        return newJourneyId
    }
}
