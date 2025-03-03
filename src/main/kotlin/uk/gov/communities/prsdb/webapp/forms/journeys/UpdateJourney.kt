package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class UpdateJourney<T : StepId>(
    journeyType: JourneyType,
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<T>(journeyType, validator, journeyDataService) {
    abstract val updateStepId: T

    protected val originalDataKey = "ORIGINAL_${journeyType.name}"

    protected abstract fun createOriginalJourneyData(updateEntityId: String): JourneyData

    protected open fun initialiseJourneyDataIfNotInitialised(
        updateEntityId: String,
        journeyDataKey: String? = null,
    ) {
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyDataKey ?: defaultJourneyDataKey)
        if (!isJourneyDataInitialised(journeyData)) {
            val newJourneyData = journeyData + (originalDataKey to createOriginalJourneyData(updateEntityId))
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
    }

    fun populateModelAndGetViewNameForUpdateStep(
        updateEntityId: String,
        model: Model,
        subPageNumber: Int? = null,
        submittedPageData: PageData? = null,
        journeyDataKey: String? = null,
    ): String {
        initialiseJourneyDataIfNotInitialised(updateEntityId, journeyDataKey)
        return super.populateModelAndGetViewName(updateStepId, model, subPageNumber, submittedPageData, journeyDataKey)
    }

    override fun getUnreachableStepRedirect(journeyData: JourneyData) =
        if (!isJourneyDataInitialised(journeyData)) {
            updateStepId.urlPathSegment
        } else {
            last().step.id.urlPathSegment
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

    private fun isJourneyDataInitialised(journeyData: JourneyData): Boolean = journeyData.containsKey(originalDataKey)

    protected fun isJourneyDataInitialised(journeyDataKey: String? = null): Boolean {
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyDataKey ?: defaultJourneyDataKey)
        return isJourneyDataInitialised(journeyData)
    }
}
