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
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import java.util.Optional

abstract class Journey<T : StepId>(
    private val journeyType: JourneyType,
    protected val validator: Validator,
    protected val journeyDataService: JourneyDataService,
) : Iterable<StepDetails<T>> {
    abstract val initialStepId: T
    val steps: Set<Step<T>>
        get() = sections.flatMap { section -> section.tasks }.flatMap { task -> task.steps }.toSet()

    abstract val sections: List<JourneySection<T>>

    open fun getUnreachableStepRedirect(journeyData: JourneyData) = "/${journeyType.urlPathSegment}/${initialStepId.urlPathSegment}"

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
        if (!isStepReachable(requestedStep, subPageNumber)) {
            return "redirect:${getUnreachableStepRedirect(journeyData)}"
        }
        val prevStepDetails = getPrevStep(requestedStep, subPageNumber)
        val prevStepUrl = getPrevStepUrl(prevStepDetails?.step, prevStepDetails?.subPageNumber)
        val pageData =
            submittedPageData ?: JourneyDataHelper.getPageData(journeyData, requestedStep.name, subPageNumber)

        val sectionHeaderInfo = getSectionHeaderInfo(requestedStep)

        return requestedStep.page.populateModelAndGetTemplateName(
            validator,
            model,
            pageData,
            prevStepUrl,
            prevStepDetails?.filteredJourneyData,
            sectionHeaderInfo,
        )
    }

    fun getSectionHeaderInfo(step: Step<T>): SectionHeaderViewModel? {
        val sectionContainingStep = sections.single { it.isStepInSection(step.id) }
        if (sectionContainingStep.headingKey == null) {
            return null
        }
        return SectionHeaderViewModel(
            sectionContainingStep.headingKey,
            sections.indexOf(sectionContainingStep) + 1,
            sections.size,
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
        val newJourneyData = currentStep.updatedJourneyData(journeyData, pageData, subPageNumber)
        journeyDataService.setJourneyData(newJourneyData)

        if (currentStep.saveAfterSubmit) {
            val journeyDataContextId = journeyDataService.getContextId()
            journeyDataService.saveJourneyData(journeyDataContextId, newJourneyData, journeyType, principal)
        }

        if (currentStep.handleSubmitAndRedirect != null) {
            return "redirect:${currentStep.handleSubmitAndRedirect!!(newJourneyData, subPageNumber)}"
        }
        val (newStepId: StepId?, newSubPageNumber: Int?) = currentStep.nextAction(newJourneyData, subPageNumber)
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
        targetStep: Step<T>,
        targetSubPageNumber: Int? = null,
    ): Boolean {
        // Initial page is always reachable
        if (targetStep.id == initialStepId) return true
        // All other steps are reachable if and only if we can find their previous step by traversal
        return getPrevStep(targetStep, targetSubPageNumber) != null
    }

    private fun getPrevStep(
        targetStep: Step<T>,
        targetSubPageNumber: Int?,
    ): StepDetails<T>? =
        zipWithNext()
            .singleOrNull { (_, next) -> next.step == targetStep && next.subPageNumber == targetSubPageNumber }
            ?.first

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

    fun getTaskStatus(
        task: JourneyTask<T>,
        journeyData: JourneyData,
    ): TaskStatus {
        val canTaskBeStarted = isStepReachable(task.steps.single { it.id == task.startingStepId })
        return task.getTaskStatus(journeyData, validator, canTaskBeStarted)
    }

    protected fun <T : StepId> createSingleSectionWithSingleTaskFromSteps(
        initialStepId: T,
        steps: Set<Step<T>>,
    ): List<JourneySection<T>> = listOf(JourneySection.withOneTask(JourneyTask(initialStepId, steps)))

    override fun iterator(): Iterator<StepDetails<T>> =
        ReachableStepDetailsIterator(journeyDataService.getJourneyDataFromSession(), steps, initialStepId, validator)
}
