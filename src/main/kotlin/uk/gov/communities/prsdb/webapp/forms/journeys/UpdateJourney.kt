package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.ReachableStepDetailsIterator
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class UpdateJourney<T : StepId>(
    journeyType: JourneyType,
    journeyPathSegment: String,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val updateStepId: T,
) : Journey<T>(journeyType, journeyPathSegment, initialStepId, validator, journeyDataService) {
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

    fun getModelAndViewForUpdateStep(
        updateEntityId: String,
        subPageNumber: Int? = null,
        submittedPageData: PageData? = null,
        journeyDataKey: String? = null,
    ): ModelAndView {
        initialiseJourneyDataIfNotInitialised(updateEntityId, journeyDataKey)
        return super.getModelAndViewForStep(
            updateStepId.urlPathSegment,
            subPageNumber,
            submittedPageData,
            journeyDataKey,
        )
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
