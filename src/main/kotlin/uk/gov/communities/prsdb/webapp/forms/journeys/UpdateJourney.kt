package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
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

    protected fun throwIfSubmittedDataIsAnInvalidUpdate(submittedData: JourneyData) {
        val journeyDataForUpdatedEntity = createOriginalJourneyData() + submittedData

        val lastStepReachedAfterUpdate = getLastReachableStepForJourneyData(journeyDataForUpdatedEntity)

        if (!lastStepReachedAfterUpdate.isSatisfiedByJourneyData(journeyDataForUpdatedEntity)) {
            throw PrsdbWebException(
                "${this::class.simpleName} journeyData would update to an invalid state: " +
                    "step '${lastStepReachedAfterUpdate.step.name}' is not satisfied by the provided data",
            )
        }

        if (!areAllSubmittedDataOnRouteToLastStep(submittedData, lastStepReachedAfterUpdate)) {
            val erroneouslyIncludedSteps = getStepsSubmittedButNotOnRoute(submittedData, lastStepReachedAfterUpdate).keys
            throw PrsdbWebException(
                "${this::class.simpleName} journeyData would update property for an unreached step. " +
                    "Erroneously included steps: ${erroneouslyIncludedSteps.joinToString("\n* ", "\n* ")}",
            )
        }
    }

    private fun StepDetails<T>.isSatisfiedByJourneyData(journeyDataForUpdatedEntity: Map<String, Any?>): Boolean {
        val subPageData =
            JourneyDataHelper.getPageData(journeyDataForUpdatedEntity, this.step.name, this.subPageNumber)
        val bindingResult = this.step.page.bindDataToFormModel(validator, subPageData)
        return subPageData != null && this.step.isSatisfied(bindingResult)
    }

    private fun areAllSubmittedDataOnRouteToLastStep(
        submittedData: JourneyData,
        lastStep: StepDetails<T>,
    ): Boolean = submittedData.filterKeys { it != originalDataKey }.all { it.value == lastStep.filteredJourneyData[it.key] }

    private fun getStepsSubmittedButNotOnRoute(
        submittedData: JourneyData,
        lastStep: StepDetails<T>,
    ): JourneyData = submittedData.filterKeys { it != originalDataKey }.filter { it.value != lastStep.filteredJourneyData[it.key] }

    private fun getLastReachableStepForJourneyData(journeyDataForUpdatedEntity: JourneyData): StepDetails<T> {
        val iterableJourney =
            object : Iterable<StepDetails<T>> {
                override fun iterator() = ReachableStepDetailsIterator(journeyDataForUpdatedEntity, steps, initialStepId, validator)
            }

        return iterableJourney.last()
    }
}
