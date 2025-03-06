package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.ReachableStepDetailsIterator
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class UpdateJourney<T : StepId>(
    journeyType: JourneyType,
    journeyDataKey: String,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val updateStepId: T,
) : Journey<T>(journeyType, journeyDataKey, initialStepId, validator, journeyDataService) {
    protected val originalDataKey = "ORIGINAL_$journeyDataKey"

    override val unreachableStepRedirect get() = last().step.id.urlPathSegment

    protected abstract fun createOriginalJourneyData(updateEntityId: String): JourneyData

    open fun initializeJourneyDataIfNotInitialized(updateEntityId: String) {
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyDataKey)
        if (!isJourneyDataInitialised(journeyData)) {
            val newJourneyData = journeyData + (originalDataKey to createOriginalJourneyData(updateEntityId))
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
    }

    override fun iterator(): Iterator<StepDetails<T>> {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val originalData = JourneyDataHelper.getPageData(journeyData, originalDataKey)

        // For any fields where the data is updated, replace the original value with the new value
        val updatedData =
            journeyData.keys
                .union(originalData?.keys ?: setOf())
                .map { key ->
                    key to if (journeyData.containsKey(key)) journeyData[key] else originalData?.get(key)
                }.associate { it }

        return ReachableStepDetailsIterator(updatedData, steps, initialStepId, validator)
    }

    protected fun isJourneyDataInitialised(): Boolean {
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyDataKey)
        return isJourneyDataInitialised(journeyData)
    }

    private fun isJourneyDataInitialised(journeyData: JourneyData): Boolean = journeyData.containsKey(originalDataKey)
}
