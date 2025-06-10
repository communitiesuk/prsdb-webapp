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
import java.security.Principal

abstract class UpdateJourney<T : StepId>(
    journeyType: JourneyType,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
    protected val stepName: String,
) : Journey<T>(journeyType, initialStepId, validator, journeyDataService) {
    companion object {
        fun getOriginalJourneyDataKey(journeyDataService: JourneyDataService) = "ORIGINAL_${journeyDataService.journeyDataKey}"
    }

    protected val originalDataKey = getOriginalJourneyDataKey(journeyDataService)

    abstract override val unreachableStepRedirect: String

    protected abstract fun createOriginalJourneyData(): JourneyData

    fun initializeOriginalJourneyDataIfNotInitialized() {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (!isOriginalJourneyDataInitialised(journeyData)) {
            val newJourneyData = journeyData + (originalDataKey to createOriginalJourneyData())
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
    }

    fun getModelAndViewForStep(submittedPageData: PageData? = null): ModelAndView =
        getModelAndViewForStep(stepName, null, submittedPageData, null)

    fun completeStep(
        formData: PageData,
        principal: Principal,
    ): ModelAndView = completeStep(stepName, formData, null, principal, null)

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

    private fun isOriginalJourneyDataInitialised(journeyData: JourneyData): Boolean = journeyData.containsKey(originalDataKey)
}
