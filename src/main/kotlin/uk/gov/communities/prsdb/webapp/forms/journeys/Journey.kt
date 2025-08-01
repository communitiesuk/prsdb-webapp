package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
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
import kotlin.reflect.cast

abstract class Journey<T : StepId>(
    protected val journeyType: JourneyType,
    val initialStepId: T,
    protected val validator: Validator,
    protected val journeyDataService: JourneyDataService,
) : Iterable<StepDetails<T>> {
    abstract val sections: List<JourneySection<T>>

    protected open val stepRouter: StepRouter<T> = IsolatedStepRouter()

    protected val steps: Set<Step<T>>
        get() = sections.flatMap { section -> section.tasks }.flatMap { task -> task.steps }.toSet()

    protected open val unreachableStepRedirect = initialStepId.urlPathSegment

    protected open val checkYourAnswersStepId: T? = null

    fun getModelAndViewForStep(
        stepPathSegment: String,
        subPageNumber: Int?,
        submittedPageData: PageData? = null,
        checkingAnswersForStep: String? = null,
    ): ModelAndView {
        val requestedStep = getStep(stepPathSegment)
        if (!isStepReachable(requestedStep, subPageNumber)) {
            return ModelAndView("redirect:$unreachableStepRedirect")
        }
        val prevStepDetails = getPrevStep(requestedStep, subPageNumber)
        val prevStepUrl = buildPreviousStepUrl(prevStepDetails, checkingAnswersForStep?.let { getStep(it) }?.id)
        val pageData =
            submittedPageData
                ?: JourneyDataHelper.getPageData(journeyDataService.getJourneyDataFromSession(), requestedStep.name, subPageNumber)
        val bindingResult = requestedStep.page.bindDataToFormModel(validator, pageData)

        val sectionHeaderInfo = getSectionHeaderInfo(requestedStep)

        return requestedStep.page.getModelAndView(
            bindingResult,
            prevStepUrl,
            prevStepDetails?.filteredJourneyData,
            sectionHeaderInfo,
        )
    }

    fun completeStep(
        stepPathSegment: String,
        formData: PageData,
        subPageNumber: Int?,
        principal: Principal,
        checkingAnswersForStep: String? = null,
    ): ModelAndView {
        val currentStep = getStep(stepPathSegment)

        val filteredJourneyData = getPrevStep(currentStep, subPageNumber)?.filteredJourneyData ?: emptyMap()
        val bindingResult = currentStep.page.bindDataToFormModel(validator, formData)

        if (!currentStep.isSatisfied(bindingResult)) {
            return getModelAndViewForStep(
                stepPathSegment,
                subPageNumber,
                formData,
            )
        }

        val formModel = currentStep.page.formModel.cast(bindingResult.target)

        val newStepDataPair = currentStep.stepDataPair(filteredJourneyData, formModel, subPageNumber)
        val newFilteredJourneyData = filteredJourneyData + newStepDataPair
        journeyDataService.addToJourneyDataIntoSession(mapOf(newStepDataPair))

        if (currentStep.saveAfterSubmit) {
            val journeyDataContextId = journeyDataService.getContextId()
            val journeyData = journeyDataService.getJourneyDataFromSession()
            journeyDataService.saveJourneyData(journeyDataContextId, journeyData, journeyType, principal)
        }

        val checkingAnswersForId = checkingAnswersForStep?.let { getStep(it).id }
        if (currentStep.handleSubmitAndRedirect != null) {
            return ModelAndView(
                "redirect:${currentStep.handleSubmitAndRedirect.invoke(newFilteredJourneyData, subPageNumber, checkingAnswersForId)}",
            )
        }

        val redirectUrl = getRedirectForNextStep(currentStep, newFilteredJourneyData, subPageNumber, checkingAnswersForId)
        return ModelAndView("redirect:$redirectUrl")
    }

    private fun buildPreviousStepUrl(
        prevStepDetails: StepDetails<T>?,
        checkingAnswersFor: T?,
    ): String? =
        if (checkingAnswersFor == null ||
            stepRouter.isDestinationAllowedWhenCheckingAnswersFor(prevStepDetails?.step?.id, checkingAnswersFor)
        ) {
            prevStepDetails?.let { Step.generateUrl(it.step.id, it.subPageNumber, checkingAnswersFor) }
        } else {
            checkYourAnswersStepId?.let { Step.generateUrl(it, null, null) }
        }

    protected fun getRedirectForNextStep(
        currentStep: Step<T>,
        filteredJourneyData: JourneyData,
        subPageNumber: Int?,
        checkingAnswersFor: T? = null,
        overriddenRedirectStepId: T? = null,
        overriddenRedirectSubPageNumber: Int? = null,
    ): String {
        val (newStepId: T?, newSubPageNumber: Int?) =
            if (overriddenRedirectStepId == null) {
                currentStep.nextAction(filteredJourneyData, subPageNumber)
            } else {
                Pair(overriddenRedirectStepId, overriddenRedirectSubPageNumber)
            }

        return if (checkingAnswersFor == null || stepRouter.isDestinationAllowedWhenCheckingAnswersFor(newStepId, checkingAnswersFor)) {
            if (newStepId == null) {
                throw IllegalStateException("Cannot compute next step from step ${currentStep.id.urlPathSegment}")
            }
            Step.generateUrl(newStepId, newSubPageNumber, checkingAnswersFor)
        } else {
            // Assigning to localCheckYourAnswersStep allows the null check here to smart cast from T? to T
            val localCheckYourAnswersStep =
                checkYourAnswersStepId
                    ?: throw IllegalStateException("No check your answers step defined for journey ${journeyType.name}")
            Step.generateUrl(localCheckYourAnswersStep, null, null)
        }
    }

    override fun iterator(): Iterator<StepDetails<T>> =
        ReachableStepDetailsIterator(journeyDataService.getJourneyDataFromSession(), steps, initialStepId, validator)

    protected fun isStepReachable(
        targetStep: Step<T>,
        targetSubPageNumber: Int? = null,
    ): Boolean = this.any { it.step == targetStep && it.subPageNumber == targetSubPageNumber }

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
}
