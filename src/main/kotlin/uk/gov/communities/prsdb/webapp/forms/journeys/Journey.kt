package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import java.util.Optional

abstract class Journey<T : StepId>(
    private val journeyType: JourneyType,
    protected val validator: Validator,
    protected val journeyDataService: JourneyDataService,
) {
    abstract val initialStepId: T
    val steps: Set<Step<T>>
        get() = sections.flatMap { section -> section.tasks }.flatMap { task -> task.steps }.toSet()

    abstract val sections: List<JourneySection<T>>

    open val unreachableStepRedirect
        get() = "/${journeyType.urlPathSegment}/${initialStepId.urlPathSegment}"

    fun getStepId(stepName: String): StepId {
        val step = steps.singleOrNull { step -> step.id.urlPathSegment == stepName }
        if (step == null) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step $stepName not valid for journey ${journeyType.urlPathSegment}",
            )
        }
        return step.id
    }

    fun populateModelAndGetViewName(
        stepId: StepId,
        model: Model,
        subPageNumber: Int?,
        submittedPageData: PageData? = null,
    ): String {
        val journeyData: JourneyData = journeyDataService.getJourneyDataFromSession()
        val requestedStep =
            steps.singleOrNull { step -> step.id == stepId } ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step ${stepId.urlPathSegment} not valid for journey ${journeyType.urlPathSegment}",
            )
        if (!isStepReachable(journeyData, requestedStep, subPageNumber)) {
            return "redirect:$unreachableStepRedirect"
        }
        val prevStepDetails = getPrevStep(journeyData, requestedStep, subPageNumber)
        val prevStepUrl = getPrevStepUrl(prevStepDetails?.step, prevStepDetails?.subPageNumber)
        val pageData =
            submittedPageData ?: JourneyDataHelper.getPageData(journeyData, requestedStep.name, subPageNumber)
        return requestedStep.page.populateModelAndGetTemplateName(
            validator,
            model,
            pageData,
            prevStepUrl,
            prevStepDetails?.filteredJourneyData,
        )
    }

    fun updateJourneyDataAndGetViewNameOrRedirect(
        stepId: StepId,
        pageData: PageData,
        model: Model,
        subPageNumber: Int?,
        principal: Principal,
    ): String {
        val currentStep =
            steps.singleOrNull { step -> step.id == stepId } ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step ${stepId.urlPathSegment} not valid for journey ${journeyType.urlPathSegment}",
            )
        if (!currentStep.isSatisfied(validator, pageData)) {
            return populateModelAndGetViewName(stepId, model, subPageNumber, pageData)
        }
        val journeyData = journeyDataService.getJourneyDataFromSession()
        currentStep.updateJourneyData(journeyData, pageData, subPageNumber)
        journeyDataService.setJourneyData(journeyData)

        if (currentStep.saveAfterSubmit) {
            val journeyDataContextId = journeyDataService.getContextId()
            journeyDataService.saveJourneyData(journeyDataContextId, journeyData, journeyType, principal)
        }

        if (currentStep.handleSubmitAndRedirect != null) {
            return "redirect:${currentStep.handleSubmitAndRedirect!!(journeyData, subPageNumber)}"
        }
        val (newStepId: StepId?, newSubPageNumber: Int?) = currentStep.nextAction(journeyData, subPageNumber)
        if (newStepId == null) {
            throw IllegalStateException("Cannot compute next step from step ${currentStep.id.urlPathSegment}")
        }
        val redirectUrl =
            UriComponentsBuilder
                .newInstance()
                .path("/${journeyType.urlPathSegment}/${newStepId.urlPathSegment}")
                .queryParamIfPresent("subpage", Optional.ofNullable(newSubPageNumber))
                .build(true)
                .toUriString()
        return "redirect:$redirectUrl"
    }

    fun isStepReachable(
        journeyData: JourneyData,
        targetStep: Step<T>,
        targetSubPageNumber: Int? = null,
    ): Boolean {
        // Initial page is always reachable
        if (targetStep.id == initialStepId) return true
        // All other steps are reachable if and only if we can find their previous step by traversal
        return getPrevStep(journeyData, targetStep, targetSubPageNumber) != null
    }

    private fun getPrevStep(
        journeyData: JourneyData,
        targetStep: Step<T>,
        targetSubPageNumber: Int?,
    ): StepDetails<T>? {
        val initialStep = steps.singleOrNull { step -> step.id == initialStepId } ?: return null
        var currentStep = initialStep
        var prevStep: Step<T>? = null
        var prevSubPageNumber: Int? = null
        var currentSubPageNumber: Int? = null
        val filteredJourneyData: JourneyData = mutableMapOf()
        while (!(currentStep.id == targetStep.id && currentSubPageNumber == targetSubPageNumber)) {
            val pageData = JourneyDataHelper.getPageData(journeyData, currentStep.name, currentSubPageNumber)
            if (pageData == null || !currentStep.isSatisfied(validator, pageData)) return null

            // This stores journeyData for only the journey path the user is on
            // and excludes user data for pages in the journey that belong to a different path
            val stepData = JourneyDataHelper.getPageData(journeyData, currentStep.name, null)
            filteredJourneyData[currentStep.name] = stepData

            val (nextStepId, nextSubPageNumber) =
                currentStep.nextAction(journeyData, currentSubPageNumber)
            val nextStep = steps.singleOrNull { step -> step.id == nextStepId } ?: return null
            prevStep = currentStep
            prevSubPageNumber = currentSubPageNumber
            currentStep = nextStep
            currentSubPageNumber = nextSubPageNumber
        }
        return StepDetails(prevStep, prevSubPageNumber, filteredJourneyData)
    }

    private fun getPrevStepUrl(
        prevStep: Step<T>?,
        prevSubPageNumber: Int?,
    ): String? {
        if (prevStep == null) return null
        val optionalPrevSubPageNumber = Optional.ofNullable(prevSubPageNumber)
        return UriComponentsBuilder
            .newInstance()
            .path(prevStep.id.urlPathSegment)
            .queryParamIfPresent("subpage", optionalPrevSubPageNumber)
            .build(true)
            .toUriString()
    }

    fun initialiseJourneyDataIfNotInitialised(principalName: String) {
        val data = journeyDataService.getJourneyDataFromSession()
        if (data.isEmpty()) {
            /* TODO PRSD-589 Currently this looks the context up from the database,
                takes the id, then passes the id to another method which retrieves it
                from the database. When this is reworked, we should just pass the whole
                context to an overload of journeyDataService.loadJourneyDataIntoSession().*/
            val contextId = journeyDataService.getContextId(principalName, journeyType)
            if (contextId == null) {
                oneTimeInitialisation(data)
            } else {
                journeyDataService.loadJourneyDataIntoSession(contextId)
            }
        }
    }

    open fun oneTimeInitialisation(journeyData: JourneyData) {}

    fun getTaskStatus(
        task: JourneyTask<T>,
        journeyData: JourneyData,
    ): TaskStatus =
        if (isStepReachable(journeyData, task.steps.single { it.id == task.startingStepId })) {
            task.getTaskStatus(journeyData, validator)
        } else {
            TaskStatus.CANNOT_START_YET
        }

    protected fun <T : StepId> unitarySetOfSteps(
        initialStepId: T,
        steps: Set<Step<T>>,
    ): List<JourneySection<T>> = listOf(JourneySection.withOneTask(JourneyTask(initialStepId, steps)))
}
