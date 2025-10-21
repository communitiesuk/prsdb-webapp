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

class JourneyStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState>(
    val stepConfig: AbstractStepConfig<TEnum, TFormModel, TState>,
) {
    fun beforeIsStepReachable() {
        stepConfig.beforeIsStepReachable(state)
    }

    fun afterIsStepReached() {
        stepConfig.afterIsStepReached(state)
    }

    fun beforeValidateSubmittedData(formData: PageData): PageData = stepConfig.beforeValidateSubmittedData(formData, state)

    fun afterValidateSubmittedData(bindingResult: BindingResult) = stepConfig.afterValidateSubmittedData(bindingResult, state)

    fun beforeGetStepContent() {
        stepConfig.beforeGetStepContent(state)
    }

    fun chooseTemplate(): String = stepConfig.chooseTemplate(state)

    fun afterGetStepContent() {
        stepConfig.afterGetStepContent(state)
    }

    fun beforeGetTemplate() {
        stepConfig.beforeGetTemplate(state)
    }

    fun afterGetTemplate() {
        stepConfig.afterGetTemplate(state)
    }

    fun beforeSubmitFormData() {
        stepConfig.beforeSubmitFormData(state)
    }

    fun afterSubmitFormData() {
        stepConfig.afterSubmitFormData(state)
    }

    fun beforeDetermineRedirect() {
        stepConfig.beforeDetermineRedirect(state)
    }

    fun afterDetermineRedirect() {
        stepConfig.afterDetermineRedirect(state)
    }

    val isStepReachable: Boolean
        get() = parentage.allowsChild()

    fun validateSubmittedData(formData: PageData): BindingResult =
        formData.let {
            val binder = WebDataBinder(stepConfig.formModelClass.createInstance())
            binder.validator = stepConfig.validator
            binder.bind(MutablePropertyValues(it))
            binder.validate()
            binder.bindingResult
        }

    fun getPageVisitContent() =
        stepConfig.getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                "formModel" to (formModel ?: stepConfig.formModelClass.createInstance()),
            )

    fun getInvalidSubmissionContent(bindingResult: BindingResult) =
        stepConfig.getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult,
            )

    fun submitFormData(bindingResult: BindingResult) {
        state.addStepData(stepConfig.routeSegment, stepConfig.formModelClass.cast(bindingResult.target).toPageData())
    }

    fun determineRedirect(): String = stepConfig.mode(state)?.let { redirectToUrl(it) } ?: routeSegment

    private lateinit var unreachableStepRedirect: () -> String

    fun getUnreachableStepRedirect() = unreachableStepRedirect()

    val formModel: TFormModel?
        get() = stepConfig.getFormModelFromState(state)

    lateinit var parentage: Parentage

    private lateinit var state: TState

    fun outcome(): TEnum? = if (isStepReachable)stepConfig.mode(state) else null

    private lateinit var redirectToUrl: (mode: TEnum) -> String

    private var backUrlOverride: (() -> String?)? = null

    val backUrl: String?
        get() {
            val singleParentUrl =
                parentage.allowingParentSteps
                    .singleOrNull()
                    ?.stepConfig
                    ?.routeSegment
            val backUrlOverrideValue = this.backUrlOverride?.let { it() }
            return if (backUrlOverride != null) backUrlOverrideValue else singleParentUrl
        }

    val routeSegment: String get() = stepConfig.routeSegment

    val initialisationStage: StepInitialisationStage
        get() =
            when {
                !isBaseClassInitialised -> StepInitialisationStage.UNINITIALISED
                isBaseClassInitialised && !stepConfig.isSubClassInitialised() -> StepInitialisationStage.PARTIALLY_INITIALISED
                else -> StepInitialisationStage.FULLY_INITIALISED
            }
    private val isBaseClassInitialised: Boolean
        get() =
            stepConfig.isRouteSegmentInitialised() && ::state.isInitialized && ::redirectToUrl.isInitialized &&
                ::parentage.isInitialized

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
        this.stepConfig.routeSegment = segment
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
