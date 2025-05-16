package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.ReachableStepDetailsIterator
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal

abstract class UpdateJourney<T : UpdateStepId<*>>(
    journeyType: JourneyType,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val stepName: String,
) : Journey<T>(journeyType, initialStepId, validator, journeyDataService) {
    companion object {
        fun getOriginalJourneyDataKey(journeyDataService: JourneyDataService) = "ORIGINAL_${journeyDataService.journeyDataKey}"
    }

    protected val originalDataKey = getOriginalJourneyDataKey(journeyDataService)

    abstract override val stepRouter: GroupedStepRouter<T>

    override val unreachableStepRedirect get() = last().step.id.urlPathSegment

    override val checkYourAnswersStepId: T?
        get() {
            val currentStepId = steps.singleOrNull { it.id.urlPathSegment == stepName }?.id ?: return null
            return steps.singleOrNull { it.id.isCheckYourAnswersStepId && it.id.groupIdentifier == currentStepId.groupIdentifier }?.id
        }

    protected abstract fun createOriginalJourneyData(): JourneyData

    open fun initializeJourneyDataIfNotInitialized() {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (!isJourneyDataInitialised(journeyData)) {
            val newJourneyData = journeyData + (originalDataKey to createOriginalJourneyData())
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
    }

    fun getModelAndViewForStep(changingAnswersForStep: String? = null): ModelAndView =
        getModelAndViewForStep(stepName, null, null, changingAnswersForStep)

    fun completeStep(
        formData: PageData,
        principal: Principal,
        changingAnswersForStep: String? = null,
    ): ModelAndView = completeStep(stepName, formData, null, principal, changingAnswersForStep)

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
        val journeyData = journeyDataService.getJourneyDataFromSession()
        return isJourneyDataInitialised(journeyData)
    }

    private fun isJourneyDataInitialised(journeyData: JourneyData): Boolean = journeyData.containsKey(originalDataKey)
}
