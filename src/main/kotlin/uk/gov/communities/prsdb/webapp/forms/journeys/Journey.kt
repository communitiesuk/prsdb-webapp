package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import java.util.Optional

abstract class Journey<T : StepId>(
    protected val journeyType: JourneyType,
    protected val validator: Validator,
    protected val journeyDataService: JourneyDataService,
) : Iterable<StepDetails<T>> {
    abstract val initialStepId: T

    abstract val sections: List<JourneySection<T>>

    protected val steps: Set<Step<T>>
        get() = sections.flatMap { section -> section.tasks }.flatMap { task -> task.steps }.toSet()

    protected abstract val journeyPathSegment: String

    fun getStepId(stepName: String): T {
        val step = steps.singleOrNull { step -> step.id.urlPathSegment == stepName }
        if (step == null) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step $stepName not valid for journey ${journeyType.name}",
            )
        }
        return step.id
    }

    fun loadJourneyDataIfNotLoaded(
        principalName: String,
        journeyDataKey: String? = null,
    ) {
        val data = journeyDataService.getJourneyDataFromSession(journeyDataKeyOrDefault(journeyDataKey))
        if (data.isEmpty()) {
            /* TODO PRSD-589 Currently this looks the context up from the database,
                takes the id, then passes the id to another method which retrieves it
                from the database. When this is reworked, we should just pass the whole
                context to an overload of journeyDataService.loadJourneyDataIntoSession().*/
            val contextId = journeyDataService.getContextId(principalName, journeyType)
            if (contextId != null) {
                journeyDataService.loadJourneyDataIntoSession(contextId)
            }
        }
    }

    fun populateModelAndGetViewName(
        stepId: StepId,
        model: Model,
        subPageNumber: Int?,
        submittedPageData: PageData? = null,
        journeyDataKey: String? = null,
    ): String {
        val journeyData: JourneyData =
            journeyDataService.getJourneyDataFromSession(journeyDataKeyOrDefault(journeyDataKey))
        val requestedStep = getStep(stepId)
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

    fun updateJourneyDataAndGetViewNameOrRedirect(
        stepId: T,
        pageData: PageData,
        model: Model,
        subPageNumber: Int?,
        principal: Principal,
        journeyDataKey: String? = null,
    ): String {
        val journeyDataKeyOrDefault = journeyDataKeyOrDefault(journeyDataKey)
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyDataKeyOrDefault)

        val currentStep = getStep(stepId)
        if (!currentStep.isSatisfied(validator, pageData)) {
            return populateModelAndGetViewName(stepId, model, subPageNumber, pageData, journeyDataKeyOrDefault)
        }

        val newJourneyData = currentStep.updatedJourneyData(journeyData, pageData, subPageNumber)
        journeyDataService.setJourneyDataInSession(newJourneyData)

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
                .path("/$journeyPathSegment/${newStepId.urlPathSegment}")
                .queryParamIfPresent("subpage", Optional.ofNullable(newSubPageNumber))
                .build(true)
                .toUriString()
        return "redirect:$redirectUrl"
    }

    override fun iterator(): Iterator<StepDetails<T>> =
        ReachableStepDetailsIterator(journeyDataService.getJourneyDataFromSession(), steps, initialStepId, validator)

    protected fun isStepReachable(
        targetStep: Step<T>,
        targetSubPageNumber: Int? = null,
    ): Boolean {
        // Initial page is always reachable
        if (targetStep.id == initialStepId) return true
        // All other steps are reachable if and only if we can find their previous step by traversal
        return getPrevStep(targetStep, targetSubPageNumber) != null
    }

    protected open fun getUnreachableStepRedirect(journeyData: JourneyData) = initialStepId.urlPathSegment

    protected fun <T : StepId> createSingleSectionWithSingleTaskFromSteps(
        initialStepId: T,
        steps: Set<Step<T>>,
    ): List<JourneySection<T>> = listOf(JourneySection.withOneTask(JourneyTask(initialStepId, steps)))

    protected fun journeyDataKeyOrDefault(journeyDataKey: String?) = journeyDataKey ?: journeyType.name

    private fun getSectionHeaderInfo(step: Step<T>): SectionHeaderViewModel? {
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

    private fun getStep(stepId: StepId) =
        steps.singleOrNull { step -> step.id == stepId }
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step $stepId not valid for journey ${journeyType.name}",
            )

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
}
