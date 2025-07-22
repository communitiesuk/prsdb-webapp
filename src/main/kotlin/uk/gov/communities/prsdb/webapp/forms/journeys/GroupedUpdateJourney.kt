package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.GroupedUpdateStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.security.Principal

abstract class GroupedUpdateJourney<T : GroupedUpdateStepId<*>>(
    journeyType: JourneyType,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
    stepName: String,
) : UpdateJourney<T>(journeyType, initialStepId, validator, journeyDataService, stepName) {
    abstract override val stepRouter: GroupedUpdateStepRouter<T>

    override val checkYourAnswersStepId: T?
        get() {
            val currentStepId = steps.singleOrNull { it.id.urlPathSegment == stepName }?.id ?: return null
            return steps.singleOrNull { it.id.isCheckYourAnswersStepId && it.id.groupIdentifier == currentStepId.groupIdentifier }?.id
        }

    fun getModelAndViewForStep(
        submittedPageData: PageData? = null,
        checkingAnswersForStep: String? = null,
    ): ModelAndView = getModelAndViewForStep(stepName, null, submittedPageData, checkingAnswersForStep)

    fun completeStep(
        formData: PageData,
        principal: Principal,
        checkingAnswersForStep: String? = null,
    ): ModelAndView = completeStep(stepName, formData, null, principal, checkingAnswersForStep)
}
