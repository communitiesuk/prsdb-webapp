package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.ReachableStepDetailsIterator
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import java.util.Optional

abstract class Journey<T : StepId>(
    protected val journeyType: JourneyType,
    protected val journeyPathSegment: String,
    val initialStepId: T,
    protected val validator: Validator,
    protected val journeyDataService: JourneyDataService,
) : Iterable<StepDetails<T>> {
    abstract val sections: List<JourneySection<T>>

    protected val steps: Set<Step<T>>
        get() = sections.flatMap { section -> section.tasks }.flatMap { task -> task.steps }.toSet()

    fun loadJourneyDataIfNotLoaded(principalName: String) {
        val data = journeyDataService.getJourneyDataFromSession(journeyPathSegment)
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

    fun getModelAndViewForStep(
        stepPathSegment: String,
        subPageNumber: Int?,
        submittedPageData: PageData? = null,
    ): ModelAndView {
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyPathSegment)
        val requestedStep = getStep(stepPathSegment)
        if (!isStepReachable(requestedStep, subPageNumber)) {
            return ModelAndView("redirect:${getUnreachableStepRedirect(journeyData)}")
        }
        val prevStepDetails = getPrevStep(requestedStep, subPageNumber)
        val prevStepUrl = getPrevStepUrl(prevStepDetails?.step, prevStepDetails?.subPageNumber)
        val pageData =
            submittedPageData ?: JourneyDataHelper.getPageData(journeyData, requestedStep.name, subPageNumber)

        val sectionHeaderInfo = getSectionHeaderInfo(requestedStep)

        return requestedStep.page.getModelAndView(
            validator,
            pageData,
            prevStepUrl,
            prevStepDetails?.filteredJourneyData,
            sectionHeaderInfo,
        )
    }

    fun completeStep(
        stepPathSegment: String,
        pageData: PageData,
        subPageNumber: Int?,
        principal: Principal,
    ): ModelAndView {
        val journeyData = journeyDataService.getJourneyDataFromSession(journeyPathSegment)

        val currentStep = getStep(stepPathSegment)
        if (!currentStep.isSatisfied(validator, pageData)) {
            return getModelAndViewForStep(
                stepPathSegment,
                subPageNumber,
                pageData,
            )
        }

        val newJourneyData = currentStep.updatedJourneyData(journeyData, pageData, subPageNumber)
        journeyDataService.setJourneyDataInSession(newJourneyData)

        if (currentStep.saveAfterSubmit) {
            val journeyDataContextId = journeyDataService.getContextId()
            journeyDataService.saveJourneyData(journeyDataContextId, newJourneyData, journeyType, principal)
        }

        if (currentStep.handleSubmitAndRedirect != null) {
            return ModelAndView("redirect:${currentStep.handleSubmitAndRedirect.invoke(newJourneyData, subPageNumber)}")
        }
        val (newStepId: T?, newSubPageNumber: Int?) = currentStep.nextAction(newJourneyData, subPageNumber)
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
        return ModelAndView("redirect:$redirectUrl")
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

    protected fun createSingleSectionWithSingleTaskFromSteps(
        initialStepId: T,
        steps: Set<Step<T>>,
    ): List<JourneySection<T>> = listOf(JourneySection.withOneTask(JourneyTask(initialStepId, steps)))

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

    private fun getStep(stepPathSegment: String) =
        steps.singleOrNull { step -> step.id.urlPathSegment == stepPathSegment }
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step path segment '$stepPathSegment' is not valid for journey ${journeyType.name}",
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
