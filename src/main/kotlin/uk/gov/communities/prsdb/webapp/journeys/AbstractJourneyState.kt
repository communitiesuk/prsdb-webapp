package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

abstract class AbstractJourneyState(
    private val journeyStateService: JourneyStateService,
    protected val delegateProvider: JourneyStateDelegateProvider =
        JourneyStateDelegateProvider(journeyStateService),
) : JourneyState,
    DelegateKeysOwner by delegateProvider {
    override fun getStepData(key: String): FormData? {
        JourneyStateService.accessObserver?.read(JourneyStateAccessObserver.stepKey(key))
        return objectToStringKeyedMap(journeyStateService.getSubmittedStepData()[key])
    }

    override fun addStepData(
        key: String,
        value: FormData,
    ) {
        JourneyStateService.accessObserver?.written(JourneyStateAccessObserver.stepKey(key))
        journeyStateService.addSingleStepData(key, value)
    }

    override fun clearStepData(key: String) {
        JourneyStateService.accessObserver?.written(JourneyStateAccessObserver.stepKey(key))
        journeyStateService.clearStepData(key)
    }

    override val journeyId: String
        get() = journeyStateService.journeyId

    override val journeyMetadata: JourneyMetadata
        get() = journeyStateService.journeyMetadata

    override fun deleteJourney() = journeyStateService.deleteState()

    override fun getSubmittedStepData(): Map<String, Any?> {
        JourneyStateService.accessObserver?.read(JourneyStateAccessObserver.ALL_STEP_DATA)
        return journeyStateService.getSubmittedStepData()
    }

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

    override fun setJourneyId(newJourneyId: String) = journeyStateService.setJourneyId(newJourneyId)

    override fun copyJourneyTo(newJourneyId: String) = journeyStateService.copyJourneyTo(newJourneyId)
}
