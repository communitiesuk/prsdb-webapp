package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.collections.plus
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

class JourneyStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : DynamicJourneyState>(
    val innerStep: AbstractInnerStep<TEnum, TFormModel, TState>,
) {
    fun beforeIsStepReachable() {
        innerStep.beforeIsStepReachable(state)
    }

    fun afterIsStepReached() {
        innerStep.afterIsStepReached(state)
    }

    fun beforeValidateSubmittedData(formData: PageData): PageData = innerStep.beforeValidateSubmittedData(formData, state)

    fun afterValidateSubmittedData(bindingResult: BindingResult) = innerStep.afterValidateSubmittedData(bindingResult, state)

    fun beforeGetStepContent() {
        innerStep.beforeGetStepContent(state)
    }

    fun chooseTemplate(): String = innerStep.chooseTemplate(state)

    fun afterGetStepContent() {
        innerStep.afterGetStepContent(state)
    }

    fun beforeGetTemplate() {
        innerStep.beforeGetTemplate(state)
    }

    fun afterGetTemplate() {
        innerStep.afterGetTemplate(state)
    }

    fun beforeSubmitFormData() {
        innerStep.beforeSubmitFormData(state)
    }

    fun afterSubmitFormData() {
        innerStep.afterSubmitFormData(state)
    }

    fun beforeDetermineRedirect() {
        innerStep.beforeDetermineRedirect(state)
    }

    fun afterDetermineRedirect() {
        innerStep.afterDetermineRedirect(state)
    }

    val isStepReachable: Boolean
        get() = parentage.allowsChild()

    fun validateSubmittedData(formData: PageData): BindingResult =
        formData.let {
            val binder = WebDataBinder(innerStep.formModelClazz.createInstance())
            binder.validator = innerStep.validator
            binder.bind(MutablePropertyValues(it))
            binder.validate()
            binder.bindingResult
        }

    fun getPageVisitContent() =
        innerStep.getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                "formModel" to (formModel ?: innerStep.formModelClazz.createInstance()),
            )

    fun getInvalidSubmissionContent(bindingResult: BindingResult) =
        innerStep.getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult,
            )

    fun submitFormData(bindingResult: BindingResult) {
        state.addStepData(innerStep.routeSegment, innerStep.formModelClazz.cast(bindingResult.target).toPageData())
    }

    fun determineRedirect(): String = innerStep.mode(state)?.let { redirectToUrl(it) } ?: routeSegment

    private lateinit var unreachableStepRedirect: () -> String

    fun getUnreachableStepRedirect() = unreachableStepRedirect()

    val formModel: TFormModel?
        get() = innerStep.getFormModelFromState(state)

    lateinit var parentage: Parentage

    private lateinit var state: TState

    fun outcome(): TEnum? = if (isStepReachable)innerStep.mode(state) else null

    private lateinit var redirectToUrl: (mode: TEnum) -> String

    private var backUrlOverride: (() -> String?)? = null

    val backUrl: String?
        get() {
            val singleParentUrl =
                parentage.allowingParentSteps
                    .singleOrNull()
                    ?.innerStep
                    ?.routeSegment
            val backUrlOverrideValue = this.backUrlOverride?.let { it() }
            return if (backUrlOverride != null) backUrlOverrideValue else singleParentUrl
        }

    val routeSegment: String get() = innerStep.routeSegment

    val initialisationStage: StepInitialisationStage
        get() =
            when {
                !isBaseClassInitialised -> StepInitialisationStage.UNINITIALISED
                isBaseClassInitialised && !innerStep.isSubClassInitialised() -> StepInitialisationStage.PARTIALLY_INITIALISED
                else -> StepInitialisationStage.FULLY_INITIALISED
            }
    private val isBaseClassInitialised: Boolean
        get() = innerStep.isRouteSegmentInitialised() && ::state.isInitialized && ::redirectToUrl.isInitialized && ::parentage.isInitialized

    fun initialize(
        segment: String,
        state: TState,
        backUrlProvider: (() -> String?)?,
        redirectToProvider: (mode: TEnum) -> String,
        parentageProvider: () -> Parentage,
        unreachableStepRedirectProvider: () -> String,
    ) {
        if (initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("Step $this has already been initialised")
        }
        this.innerStep.routeSegment = segment
        this.state = state
        this.backUrlOverride = backUrlProvider
        this.redirectToUrl = redirectToProvider
        this.parentage = parentageProvider()
        this.unreachableStepRedirect = unreachableStepRedirectProvider
    }
}

enum class StepInitialisationStage {
    UNINITIALISED,
    PARTIALLY_INITIALISED,
    FULLY_INITIALISED,
}
