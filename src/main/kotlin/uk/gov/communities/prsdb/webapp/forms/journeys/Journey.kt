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
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal
import java.util.Optional

abstract class Journey<T : StepId>(
    private val journeyType: JourneyType,
    val steps: List<Step<T>>,
    val initialStepId: T,
    val validator: Validator,
    val journeyDataService: JourneyDataService,
) {
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
        var journeyData: JourneyData = journeyDataService.getJourneyDataFromSession()
        var requestedStep =
            steps.singleOrNull { step -> step.id == stepId } ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Step ${stepId.urlPathSegment} not valid for journey ${journeyType.urlPathSegment}",
            )
        if (!isReachable(journeyData, requestedStep, subPageNumber)) {
            return "redirect:/${journeyType.urlPathSegment}/${initialStepId.urlPathSegment}"
        }
        val prevStepDetails = getPrevStep(journeyData, requestedStep, subPageNumber)
        val prevStepUrl = getPrevStepUrl(prevStepDetails?.step, prevStepDetails?.subPageNumber)
        var pageData =
            submittedPageData ?: journeyDataService.getPageData(journeyData, requestedStep.name, subPageNumber)
        return requestedStep.page.populateModelAndGetTemplateName(
            validator,
            model,
            pageData,
            prevStepUrl,
        )
    }

    fun updateJourneyDataAndGetViewNameOrRedirect(
        stepId: StepId,
        pageData: PageData,
        model: Model,
        subPageNumber: Int?,
        principal: Principal,
    ): String {
        var currentStep =
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

    private fun isReachable(
        journeyData: JourneyData,
        targetStep: Step<T>,
        targetSubPageNumber: Int?,
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
        var initialStep = steps.singleOrNull { step -> step.id == initialStepId } ?: return null
        var currentStep = initialStep
        var prevStep: Step<T>? = null
        var prevSubPageNumber: Int? = null
        var currentSubPageNumber: Int? = null
        while (!(currentStep.id == targetStep.id && currentSubPageNumber == targetSubPageNumber)) {
            val pageData = journeyDataService.getPageData(journeyData, currentStep.name, currentSubPageNumber)
            if (pageData == null || !currentStep.isSatisfied(validator, pageData)) return null
            val (nextStepId, nextSubPageNumber) =
                currentStep.nextAction(journeyData, currentSubPageNumber)
            val nextStep = steps.singleOrNull { step -> step.id == nextStepId } ?: return null
            prevStep = currentStep
            prevSubPageNumber = currentSubPageNumber
            currentStep = nextStep
            currentSubPageNumber = nextSubPageNumber
        }
        return StepDetails(prevStep, prevSubPageNumber)
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
}
